package com.upeu.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequest {

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @NotBlank
    @Size(max = 20)
    private String documento;

    @Size(max = 120)
    private String email;

    @Size(max = 20)
    private String telefono;

    private Integer puntos;
}
