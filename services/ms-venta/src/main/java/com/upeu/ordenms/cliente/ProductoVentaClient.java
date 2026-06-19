package com.upeu.ordenms.cliente;

import com.upeu.ordenms.dto.ProductoVentaDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-articulo")
public interface ProductoVentaClient {

    @GetMapping("/api/v1/productos/{id}")
    ProductoVentaDto obtenerProducto(@PathVariable("id") Integer id);

    @PostMapping("/api/v1/productos/{id}/descontar-stock")
    ProductoVentaDto descontarStock(@PathVariable("id") Integer id, @RequestParam("cantidad") Integer cantidad);
}
