package com.upeu.ordenms.dto;

import lombok.Data;

@Data
public class PagoRegistradoDto {

    private Long id;
    private Long ventaId;
    private Double monto;
    private MedioPago medioPago;
    private Double montoRecibido;
    private Double vuelto;
    private String estado;
}
