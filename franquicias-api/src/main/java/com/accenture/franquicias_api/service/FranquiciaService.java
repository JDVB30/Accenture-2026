package com.accenture.franquicias_api.service;

import com.accenture.franquicias_api.dto.FranquiciaDto.*;
import com.accenture.franquicias_api.exception.GlobalExceptionHandler.NotFoundException;
import com.accenture.franquicias_api.model.Franquicia;
import com.accenture.franquicias_api.model.Producto;
import com.accenture.franquicias_api.model.Sucursal;
import com.accenture.franquicias_api.repository.FranquiciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FranquiciaService {

    private final FranquiciaRepository franquiciaRepository;

    // ─── Franquicia ───────────────────────────────────────────────────────────

    public Flux<Franquicia> listarTodas() {
        log.debug("Listando todas las franquicias");
        return franquiciaRepository.findAll();
    }

    public Mono<Franquicia> crearFranquicia(FranquiciaRequest request) {
        log.debug("Creando franquicia: {}", request.getNombre());
        Franquicia franquicia = Franquicia.builder()
                .id(UUID.randomUUID().toString())
                .nombre(request.getNombre())
                .sucursales(new ArrayList<>())
                .build();
        return franquiciaRepository.save(franquicia);
    }

    public Mono<Franquicia> actualizarNombreFranquicia(String franquiciaId, NombreRequest request) {
        log.debug("Actualizando nombre de franquicia {}", franquiciaId);
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMap(franquicia -> {
                    franquicia.setNombre(request.getNombre());
                    return franquiciaRepository.save(franquicia);
                });
    }

    // ─── Sucursal ─────────────────────────────────────────────────────────────

    public Mono<Franquicia> agregarSucursal(String franquiciaId, SucursalRequest request) {
        log.debug("Agregando sucursal '{}' a franquicia {}", request.getNombre(), franquiciaId);
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMap(franquicia -> {
                    Sucursal sucursal = Sucursal.builder()
                            .nombre(request.getNombre())
                            .productos(new ArrayList<>())
                            .build();
                    franquicia.getSucursales().add(sucursal);
                    return franquiciaRepository.save(franquicia);
                });
    }

    public Mono<Franquicia> actualizarNombreSucursal(String franquiciaId, String sucursalId, NombreRequest request) {
        log.debug("Actualizando nombre de sucursal {} en franquicia {}", sucursalId, franquiciaId);
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMap(franquicia -> {
                    Sucursal sucursal = franquicia.getSucursales().stream()
                            .filter(s -> s.getId().equals(sucursalId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Sucursal no encontrada: " + sucursalId));
                    sucursal.setNombre(request.getNombre());
                    return franquiciaRepository.save(franquicia);
                });
    }

    // ─── Producto ─────────────────────────────────────────────────────────────

    public Mono<Franquicia> agregarProducto(String franquiciaId, String sucursalId, ProductoRequest request) {
        log.debug("Agregando producto '{}' a sucursal {}", request.getNombre(), sucursalId);
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMap(franquicia -> {
                    Sucursal sucursal = franquicia.getSucursales().stream()
                            .filter(s -> s.getId().equals(sucursalId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Sucursal no encontrada: " + sucursalId));

                    Producto producto = Producto.builder()
                            .nombre(request.getNombre())
                            .stock(request.getStock())
                            .build();
                    sucursal.getProductos().add(producto);
                    return franquiciaRepository.save(franquicia);
                });
    }

    public Mono<Franquicia> eliminarProducto(String franquiciaId, String sucursalId, String productoId) {
        log.debug("Eliminando producto {} de sucursal {}", productoId, sucursalId);
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMap(franquicia -> {
                    Sucursal sucursal = franquicia.getSucursales().stream()
                            .filter(s -> s.getId().equals(sucursalId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Sucursal no encontrada: " + sucursalId));

                    boolean removed = sucursal.getProductos().removeIf(p -> p.getId().equals(productoId));
                    if (!removed) {
                        throw new NotFoundException("Producto no encontrado: " + productoId);
                    }
                    return franquiciaRepository.save(franquicia);
                });
    }

    public Mono<Franquicia> actualizarStock(String franquiciaId, String sucursalId, String productoId, StockRequest request) {
        log.debug("Actualizando stock del producto {} a {}", productoId, request.getStock());
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMap(franquicia -> {
                    Sucursal sucursal = franquicia.getSucursales().stream()
                            .filter(s -> s.getId().equals(sucursalId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Sucursal no encontrada: " + sucursalId));

                    Producto producto = sucursal.getProductos().stream()
                            .filter(p -> p.getId().equals(productoId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + productoId));

                    producto.setStock(request.getStock());
                    return franquiciaRepository.save(franquicia);
                });
    }

    public Mono<Franquicia> actualizarNombreProducto(String franquiciaId, String sucursalId, String productoId, NombreRequest request) {
        log.debug("Actualizando nombre del producto {}", productoId);
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMap(franquicia -> {
                    Sucursal sucursal = franquicia.getSucursales().stream()
                            .filter(s -> s.getId().equals(sucursalId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Sucursal no encontrada: " + sucursalId));

                    Producto producto = sucursal.getProductos().stream()
                            .filter(p -> p.getId().equals(productoId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Producto no encontrado: " + productoId));

                    producto.setNombre(request.getNombre());
                    return franquiciaRepository.save(franquicia);
                });
    }

    // ─── Consulta especial ────────────────────────────────────────────────────

    public Flux<ProductoMayorStockResponse> obtenerProductoMayorStockPorSucursal(String franquiciaId) {
        log.debug("Obteniendo producto con mayor stock por sucursal para franquicia {}", franquiciaId);
        return franquiciaRepository.findById(franquiciaId)
                .switchIfEmpty(Mono.error(new NotFoundException("Franquicia no encontrada: " + franquiciaId)))
                .flatMapMany(franquicia -> Flux.fromIterable(franquicia.getSucursales()))
                .flatMap(sucursal -> {
                    if (sucursal.getProductos().isEmpty()) {
                        return Flux.empty();
                    }
                    // Por cada sucursal, encontrar el producto con mayor stock
                    return sucursal.getProductos().stream()
                            .max(Comparator.comparingInt(Producto::getStock))
                            .map(producto -> ProductoMayorStockResponse.builder()
                                    .sucursalId(sucursal.getId())
                                    .sucursalNombre(sucursal.getNombre())
                                    .productoId(producto.getId())
                                    .productoNombre(producto.getNombre())
                                    .stock(producto.getStock())
                                    .build())
                            .map(Flux::just)
                            .orElse(Flux.empty());
                });
    }
}
