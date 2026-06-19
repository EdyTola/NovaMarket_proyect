package com.upeu.ordenms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VentaDetalleResponse {

    private Long id;
    private Integer productoId;
    private String productoNombre;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
