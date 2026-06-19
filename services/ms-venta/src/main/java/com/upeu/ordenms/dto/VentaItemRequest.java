package com.upeu.ordenms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VentaItemRequest {

    @NotNull
    private Integer productoId;

    @NotNull
    @Min(1)
    private Integer cantidad;
}
