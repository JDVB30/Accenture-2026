package com.accenture.franquicias_api.repository;

import com.accenture.franquicias_api.model.Franquicia;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FranquiciaRepository extends ReactiveMongoRepository<Franquicia, String> {
    // ReactiveMongoRepository ya provee: findById, save, findAll, deleteById — todos reactivos
}
