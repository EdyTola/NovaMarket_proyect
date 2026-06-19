package com.upeu.ordenms.servicio;

import com.upeu.ordenms.cliente.PagoVentaClient;
import com.upeu.ordenms.cliente.ProductoVentaClient;
import com.upeu.ordenms.dto.CrearVentaRequest;
import com.upeu.ordenms.dto.MedioPago;
import com.upeu.ordenms.dto.PagoRegistradoDto;
import com.upeu.ordenms.dto.ProductoVentaDto;
import com.upeu.ordenms.dto.VentaDetalleResponse;
import com.upeu.ordenms.dto.VentaItemRequest;
import com.upeu.ordenms.dto.VentaResponse;
import com.upeu.ordenms.entidad.Orden;
import com.upeu.ordenms.entidad.OrdenDetalle;
import com.upeu.ordenms.evento.EventoOrden;
import com.upeu.ordenms.repositorio.OrdenRepositorio;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VentaServicio {

    private static final String ESTADO_PAGADO = "PAGADO";
    private static final String TIPO_EVENTO_ORDEN_CREADA = "orden.creada";

    private final OrdenRepositorio ordenRepositorio;
    private final ProductoVentaClient productoVentaClient;
    private final PagoVentaClient pagoVentaClient;
    private final ProductorOrden productorOrden;

    @Value("${spring.application.name}")
    private String applicationName;

    @Transactional(readOnly = true)
    public List<VentaResponse> listarVentas() {
        return ordenRepositorio.findAllByOrderByFechaVentaDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerVenta(Long id) {
        Orden orden = ordenRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));
        return toResponse(orden);
    }

    @Transactional
    public VentaResponse crearVenta(CrearVentaRequest request) {
        validarPago(request);

        double subtotal = 0;
        List<OrdenDetalle> detalles = new ArrayList<>();

        for (VentaItemRequest item : request.getItems()) {
            ProductoVentaDto producto = productoVentaClient.obtenerProducto(item.getProductoId());
            if (producto.getStock() == null || producto.getStock() < item.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para producto " + producto.getNombre());
            }
            double precio = producto.getPrecio() != null ? producto.getPrecio().doubleValue() : 0;
            double linea = precio * item.getCantidad();
            subtotal += linea;

            detalles.add(OrdenDetalle.builder()
                    .productoId(producto.getId())
                    .productoNombre(producto.getNombre())
                    .cantidad(item.getCantidad())
                    .precioUnitario(precio)
                    .subtotal(linea)
                    .build());
        }

        double descuento = request.getDescuento() != null ? request.getDescuento() : 0;
        double total = Math.max(0, subtotal - descuento);

        Orden orden = Orden.builder()
                .clienteId(request.getClienteId())
                .cajeroUsername(request.getCajeroUsername())
                .subtotal(subtotal)
                .descuento(descuento)
                .total(total)
                .estado(ESTADO_PAGADO)
                .medioPago(request.getMedioPago().name())
                .montoRecibido(request.getMontoRecibido())
                .fechaVenta(Instant.now())
                .build();

        for (OrdenDetalle detalle : detalles) {
            detalle.setOrden(orden);
            orden.getDetalles().add(detalle);
        }

        Orden guardada = ordenRepositorio.save(orden);
        guardada.setNumeroBoleta(generarNumeroBoleta(guardada.getId()));
        if (request.getMedioPago() == MedioPago.EFECTIVO && request.getMontoRecibido() != null) {
            guardada.setVuelto(Math.max(0, request.getMontoRecibido() - total));
        }
        guardada = ordenRepositorio.save(guardada);

        for (VentaItemRequest item : request.getItems()) {
            productoVentaClient.descontarStock(item.getProductoId(), item.getCantidad());
        }

        PagoRegistradoDto pago = pagoVentaClient.registrar(
                new PagoVentaClient.RegistrarPagoFeignRequest(
                        guardada.getId(),
                        total,
                        request.getMedioPago(),
                        request.getMontoRecibido()
                )
        );

        productorOrden.publicarOrdenCreada(EventoOrden.builder()
                .tipoEvento(TIPO_EVENTO_ORDEN_CREADA)
                .ordenId(guardada.getId())
                .total(guardada.getTotal())
                .estado(guardada.getEstado())
                .origen(applicationName)
                .timestamp(Instant.now().toEpochMilli())
                .build());

        VentaResponse response = toResponse(guardada);
        response.setPagoId(pago.getId());
        if (pago.getVuelto() != null) {
            response.setVuelto(pago.getVuelto());
        }
        return response;
    }

    private void validarPago(CrearVentaRequest request) {
        if (request.getMedioPago() == MedioPago.EFECTIVO) {
            if (request.getMontoRecibido() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique el monto recibido en efectivo");
            }
        }
    }

    private String generarNumeroBoleta(Long id) {
        return String.format("NM-%08d", id);
    }

    private VentaResponse toResponse(Orden orden) {
        List<VentaDetalleResponse> items = orden.getDetalles() == null ? List.of()
                : orden.getDetalles().stream()
                .map(d -> VentaDetalleResponse.builder()
                        .id(d.getId())
                        .productoId(d.getProductoId())
                        .productoNombre(d.getProductoNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .toList();

        MedioPago medioPago = null;
        if (orden.getMedioPago() != null) {
            try {
                medioPago = MedioPago.valueOf(orden.getMedioPago());
            } catch (IllegalArgumentException ignored) {
                // legacy rows
            }
        }

        return VentaResponse.builder()
                .id(orden.getId())
                .clienteId(orden.getClienteId())
                .cajeroUsername(orden.getCajeroUsername())
                .subtotal(orden.getSubtotal())
                .descuento(orden.getDescuento())
                .total(orden.getTotal())
                .estado(orden.getEstado())
                .medioPago(medioPago)
                .montoRecibido(orden.getMontoRecibido())
                .vuelto(orden.getVuelto())
                .numeroBoleta(orden.getNumeroBoleta())
                .fechaVenta(orden.getFechaVenta())
                .items(items)
                .build();
    }
}
