package com.accenture.franquicias_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sucursal {

    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    private String nombre;

    @Builder.Default
    private List<Producto> productos = new ArrayList<>();
}
