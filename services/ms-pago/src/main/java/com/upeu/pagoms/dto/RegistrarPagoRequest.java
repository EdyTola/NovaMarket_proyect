package com.upeu.pagoms.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RegistrarPagoRequest {

    @NotNull
    private Long ventaId;

    @NotNull
    @Positive
    private Double monto;

    @NotNull
    private MedioPago medioPago;

    private Double montoRecibido;
}
