package com.upeu.ordenms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CrearVentaRequest {

    private Long clienteId;

    @NotNull
    private String cajeroUsername;

    private Double descuento;

    @NotNull
    private MedioPago medioPago;

    /** Obligatorio si medioPago = EFECTIVO */
    private Double montoRecibido;

    @NotEmpty
    @Valid
    private List<VentaItemRequest> items;
}
