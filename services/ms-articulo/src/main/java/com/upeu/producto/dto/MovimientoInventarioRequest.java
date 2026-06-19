package com.upeu.producto.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovimientoInventarioRequest {

    @NotBlank
    private String tipo;

    @NotNull
    @Min(1)
    private Integer cantidad;

    private String motivo;
}
