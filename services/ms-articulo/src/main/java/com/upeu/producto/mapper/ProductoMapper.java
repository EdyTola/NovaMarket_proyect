package com.upeu.producto.mapper;

import com.upeu.producto.dto.ProductoRequest;
import com.upeu.producto.dto.ProductoResponse;
import com.upeu.producto.entity.Producto;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public Producto toEntity(ProductoRequest request) {
        if (request == null) {
            return null;
        }

        return Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .idCategoria(request.getIdCategoria())
                .precio(request.getPrecio())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .stockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 5)
                .codigoBarras(request.getCodigoBarras())
                .imagenUrl(request.getImagenUrl())
                .build();
    }

    public ProductoResponse toResponse(Producto entity) {
        if (entity == null) {
            return null;
        }

        boolean stockBajo = entity.getStock() != null && entity.getStockMinimo() != null
                && entity.getStock() <= entity.getStockMinimo();

        return ProductoResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .idCategoria(entity.getIdCategoria())
                .precio(entity.getPrecio())
                .stock(entity.getStock())
                .stockMinimo(entity.getStockMinimo())
                .codigoBarras(entity.getCodigoBarras())
                .imagenUrl(entity.getImagenUrl())
                .stockBajo(stockBajo)
                .build();
    }

    public void updateEntityFromRequest(Producto entity, ProductoRequest request) {
        entity.setNombre(request.getNombre());
        entity.setDescripcion(request.getDescripcion());
        entity.setIdCategoria(request.getIdCategoria());
        entity.setPrecio(request.getPrecio());
        if (request.getStock() != null) {
            entity.setStock(request.getStock());
        }
        if (request.getStockMinimo() != null) {
            entity.setStockMinimo(request.getStockMinimo());
        }
        entity.setCodigoBarras(request.getCodigoBarras());
        entity.setImagenUrl(request.getImagenUrl());
    }
}
