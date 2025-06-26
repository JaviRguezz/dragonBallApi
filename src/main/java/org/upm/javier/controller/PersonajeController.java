package org.upm.javier.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.upm.javier.data.model.Personaje;
import org.upm.javier.service.PersonajeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/personajes")
public class PersonajeController {

    @Autowired
    private PersonajeService service;

    @GetMapping
    public List<Personaje> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Personaje> getById(@PathVariable String id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
    public ResponseEntity<Personaje> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String jsonContent = new String(file.getBytes());
        Personaje personaje = service.createFromFile(jsonContent);
        return ResponseEntity.ok(personaje);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Personaje> update(@PathVariable String id, @RequestBody Personaje personaje) {
        return service.update(id, personaje)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return service.softDelete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/save")
    public ResponseEntity<Personaje> saveOrUpdate(@RequestBody Personaje personaje) {
        Personaje resultado = service.createOrUpdate(personaje);
        return ResponseEntity.ok(resultado);
    }
}