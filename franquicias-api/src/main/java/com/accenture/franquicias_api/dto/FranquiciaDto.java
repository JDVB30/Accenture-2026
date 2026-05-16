package com.accenture.franquicias_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─── Request DTOs ──────────────────────────────────────────────────────────────

public class FranquiciaDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FranquiciaRequest {
        @NotBlank(message = "El nombre de la franquicia es obligatorio")
        private String nombre;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SucursalRequest {
        @NotBlank(message = "El nombre de la sucursal es obligatorio")
        private String nombre;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductoRequest {
        @NotBlank(message = "El nombre del producto es obligatorio")
        private String nombre;

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        private Integer stock;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StockRequest {
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        private Integer stock;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NombreRequest {
        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;
    }

// ─── Response DTOs ─────────────────────────────────────────────────────────────

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductoMayorStockResponse {
        private String sucursalId;
        private String sucursalNombre;
        private String productoId;
        private String productoNombre;
        private int stock;
    }
}
