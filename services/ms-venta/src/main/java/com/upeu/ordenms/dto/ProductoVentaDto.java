package com.upeu.ordenms.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductoVentaDto {

    private Integer id;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
}
