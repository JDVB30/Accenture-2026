package com.accenture.franquicias_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "franquicias")
public class Franquicia {

    @Id
    private String id;

    private String nombre;

    @Builder.Default
    private List<Sucursal> sucursales = new ArrayList<>();
}
