package org.upm.javier.service;

import org.upm.javier.data.model.Personaje;

import java.util.List;
import java.util.Optional;

public interface PersonajeService {
    List<Personaje> getAll();
    Optional<Personaje> getById(String id);
    Personaje createFromFile(String jsonContent);
    Optional<Personaje> update(String id, Personaje personaje);
    Personaje createOrUpdate(Personaje input);
    boolean softDelete(String id);
}