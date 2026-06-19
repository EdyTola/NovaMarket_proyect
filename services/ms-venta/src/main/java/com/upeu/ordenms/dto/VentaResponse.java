package com.upeu.ordenms.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VentaResponse {

    private Long id;
    private Long clienteId;
    private String cajeroUsername;
    private Double subtotal;
    private Double descuento;
    private Double total;
    private String estado;
    private MedioPago medioPago;
    private Double montoRecibido;
    private Double vuelto;
    private String numeroBoleta;
    private Instant fechaVenta;
    private Long pagoId;
    private List<VentaDetalleResponse> items;
}
