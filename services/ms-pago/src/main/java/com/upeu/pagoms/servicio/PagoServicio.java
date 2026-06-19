package com.upeu.pagoms.servicio;

import com.upeu.pagoms.dto.MedioPago;
import com.upeu.pagoms.dto.PagoResponse;
import com.upeu.pagoms.dto.RegistrarPagoRequest;
import com.upeu.pagoms.entidad.Pago;
import com.upeu.pagoms.repositorio.PagoRepositorio;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PagoServicio {

    private static final String ESTADO_APROBADO = "APROBADO";

    private final PagoRepositorio pagoRepositorio;

    public List<Pago> listarPagos() {
        return pagoRepositorio.findAll();
    }

    public Optional<Pago> buscarPagoPorId(Long id) {
        return pagoRepositorio.findById(id);
    }

    public Optional<Pago> buscarPorVentaId(Long ventaId) {
        return pagoRepositorio.findByVentaId(ventaId);
    }

    @Transactional
    public PagoResponse registrar(RegistrarPagoRequest request) {
        if (pagoRepositorio.findByVentaId(request.getVentaId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La venta ya tiene un pago registrado");
        }

        double vuelto = 0;
        if (request.getMedioPago() == MedioPago.EFECTIVO) {
            if (request.getMontoRecibido() == null || request.getMontoRecibido() < request.getMonto()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "En efectivo el monto recibido debe ser mayor o igual al total");
            }
            vuelto = request.getMontoRecibido() - request.getMonto();
        }

        Pago pago = Pago.builder()
                .ventaId(request.getVentaId())
                .ordenId(request.getVentaId())
                .monto(request.getMonto())
                .medioPago(request.getMedioPago())
                .montoRecibido(request.getMedioPago() == MedioPago.EFECTIVO
                        ? request.getMontoRecibido()
                        : request.getMonto())
                .vuelto(vuelto)
                .estado(ESTADO_APROBADO)
                .fechaPago(Instant.now())
                .build();

        Pago guardado = pagoRepositorio.save(pago);
        return toResponse(guardado);
    }

    private PagoResponse toResponse(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .ventaId(pago.getVentaId() != null ? pago.getVentaId() : pago.getOrdenId())
                .monto(pago.getMonto())
                .medioPago(pago.getMedioPago())
                .montoRecibido(pago.getMontoRecibido())
                .vuelto(pago.getVuelto())
                .estado(pago.getEstado())
                .fechaPago(pago.getFechaPago())
                .build();
    }
}
