package com.upeu.producto.service.impl;

import com.upeu.producto.dto.MovimientoInventarioRequest;
import com.upeu.producto.dto.ProductoRequest;
import com.upeu.producto.dto.ProductoResponse;
import com.upeu.producto.entity.Producto;
import com.upeu.producto.exception.ResourceNotFoundException;
import com.upeu.producto.mapper.ProductoMapper;
import com.upeu.producto.repository.ProductoRepository;
import com.upeu.producto.service.ProductoService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.upeu.producto.client.RubroClient;
import com.upeu.producto.dto.CategoriaDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final RubroClient rubroClient;

    @Override
    @Transactional
    public ProductoResponse create(ProductoRequest request) {
        log.info("Iniciando creacion de producto con nombre: {} y idCategoria: {}", request.getNombre(),
                request.getIdCategoria());
        Producto producto = productoMapper.toEntity(request);
        Producto savedProducto = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", savedProducto.getId());
        return productoMapper.toResponse(savedProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> findAll() {
        log.info("Recuperando lista de productos");
        List<ProductoResponse> productos = productoRepository.findAll()
                .stream()
                .map(productoMapper::toResponse)
                .toList();
        log.info("Se encontraron {} productos", productos.size());
        return productos;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse findById(Integer id) {
        log.info("Buscando producto con ID: {}", id);
        Producto producto = getProductoById(id);
        log.info("Producto encontrado: {} (ID: {})", producto.getNombre(), id);
        return productoMapper.toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse update(Integer id, ProductoRequest request) {
        log.info("Iniciando actualizacion de producto ID: {}", id);
        Producto producto = getProductoById(id);
        productoMapper.updateEntityFromRequest(producto, request);
        Producto updatedProducto = productoRepository.save(producto);
        log.info("Producto ID: {} actualizado exitosamente", id);
        return productoMapper.toResponse(updatedProducto);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.info("Iniciando eliminacion de producto ID: {}", id);
        getProductoById(id);
        productoRepository.deleteById(id);
        log.info("Producto ID: {} eliminado exitosamente", id);
    }

    private Producto getProductoById(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto no encontrado: ID {}", id);
                    return new ResourceNotFoundException("Producto con id " + id + " no encontrado");
                });
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "catalogo", fallbackMethod = "fallbackCategoria")
    public ProductoResponse findDetalleById(Integer id) {
        log.info("[PRODUCTO] Buscando detalle de producto con ID: {}", id);

        Producto producto = getProductoById(id);
        log.info("[PRODUCTO] Consultando categoriaId={} en catalogo", producto.getIdCategoria());

        CategoriaDto categoria = rubroClient.findCategoriaById(
                producto.getIdCategoria().longValue());

        ProductoResponse base = productoMapper.toResponse(producto);
        return ProductoResponse.builder()
                .id(base.getId())
                .nombre(base.getNombre())
                .descripcion(base.getDescripcion())
                .idCategoria(base.getIdCategoria())
                .precio(base.getPrecio())
                .stock(base.getStock())
                .stockMinimo(base.getStockMinimo())
                .codigoBarras(base.getCodigoBarras())
                .imagenUrl(base.getImagenUrl())
                .stockBajo(base.getStockBajo())
                .categoria(categoria)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse findByCodigoBarras(String codigoBarras) {
        Producto producto = productoRepository.findByCodigoBarras(codigoBarras)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con codigo " + codigoBarras + " no encontrado"));
        return productoMapper.toResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> findAlertasStockBajo() {
        return productoRepository.findAlertasStockBajo().stream().map(productoMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ProductoResponse registrarMovimiento(Integer id, MovimientoInventarioRequest request) {
        Producto producto = getProductoById(id);
        int cantidad = request.getCantidad();
        String tipo = request.getTipo() != null ? request.getTipo().toUpperCase() : "";

        int nuevoStock = switch (tipo) {
            case "ENTRADA" -> producto.getStock() + cantidad;
            case "SALIDA" -> producto.getStock() - cantidad;
            default -> throw new IllegalArgumentException("Tipo de movimiento invalido: use ENTRADA o SALIDA");
        };

        if (nuevoStock < 0) {
            throw new IllegalArgumentException("Stock insuficiente para la salida");
        }

        producto.setStock(nuevoStock);
        return productoMapper.toResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponse descontarStock(Integer id, Integer cantidad) {
        MovimientoInventarioRequest request = new MovimientoInventarioRequest();
        request.setTipo("SALIDA");
        request.setCantidad(cantidad);
        request.setMotivo("Venta POS");
        return registrarMovimiento(id, request);
    }

    public ProductoResponse fallbackCategoria(Integer id, Throwable ex) {
        log.warn("[PRODUCTO] Fallback activado para producto ID {}. Motivo: {}", id, ex.getMessage());

        Producto producto = getProductoById(id);

        ProductoResponse base = productoMapper.toResponse(producto);
        return ProductoResponse.builder()
                .id(base.getId())
                .nombre(base.getNombre())
                .descripcion(base.getDescripcion())
                .idCategoria(base.getIdCategoria())
                .precio(base.getPrecio())
                .stock(base.getStock())
                .stockMinimo(base.getStockMinimo())
                .codigoBarras(base.getCodigoBarras())
                .imagenUrl(base.getImagenUrl())
                .stockBajo(base.getStockBajo())
                .categoria(null)
                .build();
    }

}
