package com.upeu.cliente.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClienteResponse {

    private Long id;
    private String nombre;
    private String documento;
    private String email;
    private String telefono;
    private Integer puntos;
}
