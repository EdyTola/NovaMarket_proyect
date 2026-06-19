package com.upeu.ordenms.controlador;

import com.upeu.ordenms.dto.CrearVentaRequest;
import com.upeu.ordenms.dto.VentaResponse;
import com.upeu.ordenms.servicio.VentaServicio;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaControlador {

    private final VentaServicio ventaServicio;

    @GetMapping
    public List<VentaResponse> listar() {
        return ventaServicio.listarVentas();
    }

    @GetMapping("/{id}")
    public VentaResponse obtener(@PathVariable Long id) {
        return ventaServicio.obtenerVenta(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaResponse crear(@Valid @RequestBody CrearVentaRequest request) {
        return ventaServicio.crearVenta(request);
    }
}
