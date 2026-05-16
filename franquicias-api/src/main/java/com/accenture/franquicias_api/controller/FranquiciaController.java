package com.accenture.franquicias_api.controller;

import com.accenture.franquicias_api.dto.FranquiciaDto.*;
import com.accenture.franquicias_api.model.Franquicia;
import com.accenture.franquicias_api.service.FranquiciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FranquiciaController {

    private final FranquiciaService franquiciaService;

    // ─── Franquicia ───────────────────────────────────────────────────────────

    /**
     * GET /api/franquicias
     * Lista todas las franquicias.
     */
    @GetMapping("/franquicias")
    public Flux<Franquicia> listarFranquicias() {
        return franquiciaService.listarTodas();
    }

    /**
     * POST /api/franquicias
     * Crea una nueva franquicia.
     * Body: { "nombre": "Franquicia A" }
     */
    @PostMapping("/franquicias")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franquicia> crearFranquicia(@Valid @RequestBody FranquiciaRequest request) {
        return franquiciaService.crearFranquicia(request);
    }

    /**
     * PUT /api/franquicias/{franquiciaId}/nombre
     * Actualiza el nombre de una franquicia. [PLUS]
     * Body: { "nombre": "Nuevo Nombre" }
     */
    @PutMapping("/franquicias/{franquiciaId}/nombre")
    public Mono<Franquicia> actualizarNombreFranquicia(
            @PathVariable String franquiciaId,
            @Valid @RequestBody NombreRequest request) {
        return franquiciaService.actualizarNombreFranquicia(franquiciaId, request);
    }

    // ─── Sucursal ─────────────────────────────────────────────────────────────

    /**
     * POST /api/franquicias/{franquiciaId}/sucursales
     * Agrega una nueva sucursal a una franquicia.
     * Body: { "nombre": "Sucursal Norte" }
     */
    @PostMapping("/franquicias/{franquiciaId}/sucursales")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franquicia> agregarSucursal(
            @PathVariable String franquiciaId,
            @Valid @RequestBody SucursalRequest request) {
        return franquiciaService.agregarSucursal(franquiciaId, request);
    }

    /**
     * PUT /api/franquicias/{franquiciaId}/sucursales/{sucursalId}/nombre
     * Actualiza el nombre de una sucursal. [PLUS]
     * Body: { "nombre": "Nuevo Nombre" }
     */
    @PutMapping("/franquicias/{franquiciaId}/sucursales/{sucursalId}/nombre")
    public Mono<Franquicia> actualizarNombreSucursal(
            @PathVariable String franquiciaId,
            @PathVariable String sucursalId,
            @Valid @RequestBody NombreRequest request) {
        return franquiciaService.actualizarNombreSucursal(franquiciaId, sucursalId, request);
    }

    // ─── Producto ─────────────────────────────────────────────────────────────

    /**
     * POST /api/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos
     * Agrega un nuevo producto a una sucursal.
     * Body: { "nombre": "Producto X", "stock": 100 }
     */
    @PostMapping("/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Franquicia> agregarProducto(
            @PathVariable String franquiciaId,
            @PathVariable String sucursalId,
            @Valid @RequestBody ProductoRequest request) {
        return franquiciaService.agregarProducto(franquiciaId, sucursalId, request);
    }

    /**
     * DELETE /api/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos/{productoId}
     * Elimina un producto de una sucursal.
     */
    @DeleteMapping("/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos/{productoId}")
    public Mono<Franquicia> eliminarProducto(
            @PathVariable String franquiciaId,
            @PathVariable String sucursalId,
            @PathVariable String productoId) {
        return franquiciaService.eliminarProducto(franquiciaId, sucursalId, productoId);
    }

    /**
     * PUT /api/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos/{productoId}/stock
     * Modifica el stock de un producto.
     * Body: { "stock": 250 }
     */
    @PutMapping("/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos/{productoId}/stock")
    public Mono<Franquicia> actualizarStock(
            @PathVariable String franquiciaId,
            @PathVariable String sucursalId,
            @PathVariable String productoId,
            @Valid @RequestBody StockRequest request) {
        return franquiciaService.actualizarStock(franquiciaId, sucursalId, productoId, request);
    }

    /**
     * PUT /api/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos/{productoId}/nombre
     * Actualiza el nombre de un producto. [PLUS]
     * Body: { "nombre": "Nuevo Nombre" }
     */
    @PutMapping("/franquicias/{franquiciaId}/sucursales/{sucursalId}/productos/{productoId}/nombre")
    public Mono<Franquicia> actualizarNombreProducto(
            @PathVariable String franquiciaId,
            @PathVariable String sucursalId,
            @PathVariable String productoId,
            @Valid @RequestBody NombreRequest request) {
        return franquiciaService.actualizarNombreProducto(franquiciaId, sucursalId, productoId, request);
    }

    // ─── Consulta especial ────────────────────────────────────────────────────

    /**
     * GET /api/franquicias/{franquiciaId}/producto-mayor-stock
     * Retorna el producto con mayor stock por sucursal para una franquicia dada.
     * Response: lista de { sucursalId, sucursalNombre, productoId, productoNombre, stock }
     */
    @GetMapping("/franquicias/{franquiciaId}/producto-mayor-stock")
    public Flux<ProductoMayorStockResponse> obtenerProductoMayorStock(
            @PathVariable String franquiciaId) {
        return franquiciaService.obtenerProductoMayorStockPorSucursal(franquiciaId);
    }
}
