package org.upm.javier.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.upm.javier.data.model.Personaje;

import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class PersonajeServiceImpl implements PersonajeService {

    private static final String DB_PATH = "src/main/resources/characters.json";
    private final Gson gson = new Gson();

    private List<Personaje> loadPersonajes() {
        try (Reader reader = new FileReader(DB_PATH, StandardCharsets.UTF_8)) {
            Map<String, List<Personaje>> wrapper = gson.fromJson(reader, Map.class);
            Type listType = new TypeToken<List<Personaje>>() {}.getType();
            String json = gson.toJson(wrapper.get("items"));
            return gson.fromJson(json, listType);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void savePersonajes(List<Personaje> personajes) {
        Map<String, List<Personaje>> wrapper = new HashMap<>();
        wrapper.put("items", personajes);
        try (Writer writer = new FileWriter(DB_PATH, StandardCharsets.UTF_8)) {
            gson.toJson(wrapper, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    @Override
    public List<Personaje> getAll() {
        return loadPersonajes();
    }

    @Override
    public Optional<Personaje> getById(String id) {
        return loadPersonajes().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public Personaje createFromFile(String jsonContent) {
        Personaje personaje = gson.fromJson(jsonContent, Personaje.class);
        List<Personaje> personajes = loadPersonajes();
        personajes.add(personaje);
        savePersonajes(personajes);
        return personaje;
    }

    @Override
    public Optional<Personaje> update(String id, Personaje newData) {
        List<Personaje> personajes = loadPersonajes();
        for (int i = 0; i < personajes.size(); i++) {
            if (personajes.get(i).getId().equals(id)) {
                personajes.set(i, newData);
                savePersonajes(personajes);
                return Optional.of(newData);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean softDelete(String id) {
        List<Personaje> personajes = loadPersonajes();
        for (Personaje p : personajes) {
            if (p.getId().equals(id)) {
                p.setDeletedAt(LocalDate.now().toString());
                savePersonajes(personajes);
                return true;
            }
        }
        return false;
    }

    @Override
    public Personaje createOrUpdate(Personaje input) {
        List<Personaje> personajes = loadPersonajes();

        if (input.getId() == null || input.getId().isEmpty()) {
            // Nuevo personaje → le asignamos un UUID
            input.setId(UUID.randomUUID().toString());
            personajes.add(input);
        } else {
            // Buscar si existe
            boolean updated = false;
            for (int i = 0; i < personajes.size(); i++) {
                if (personajes.get(i).getId().equals(input.getId())) {
                    personajes.set(i, input); // actualizar
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                personajes.add(input); // no existía → agregarlo
            }
        }

        savePersonajes(personajes);
        return input;
    }
}
