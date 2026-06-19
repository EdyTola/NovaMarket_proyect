package com.upeu.pagoms.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagoResponse {

    private Long id;
    private Long ventaId;
    private Double monto;
    private MedioPago medioPago;
    private Double montoRecibido;
    private Double vuelto;
    private String estado;
    private Instant fechaPago;
}
