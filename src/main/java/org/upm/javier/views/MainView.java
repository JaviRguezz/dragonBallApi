package org.upm.javier.views;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.upm.javier.data.model.Personaje;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    private final Grid<Personaje> grid = new Grid<>(Personaje.class);
    private final Gson gson = new Gson();
    private final String apiUrl = "http://localhost:8080/personajes";
    private final HttpClient client = HttpClient.newHttpClient();

    public MainView() {
        Button addButton = new Button("Añadir Personaje", e -> mostrarFormulario(null));
        add(addButton);

        configurarGrid();
        cargarPersonajes();

        add(grid);
    }

    private void configurarGrid() {
        grid.setColumns("name", "ki", "maxKi", "affiliation");
        grid.addComponentColumn(this::botonesAcciones).setHeader("Acciones");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setClassNameGenerator(p -> p.getDeletedAt() != null ? "deleted" : "");
    }

    private HorizontalLayout botonesAcciones(Personaje personaje) {
        Button detalle = new Button("Ver Detalles", e -> mostrarDetalle(personaje));
        return new HorizontalLayout(detalle);
    }

    private void mostrarDetalle(Personaje personaje) {
        Dialog dialog = new Dialog();
        VerticalLayout layout = new VerticalLayout();
        layout.add(new Image(personaje.getImage(), "Imagen"));
        layout.add("Nombre: " + personaje.getName());
        layout.add("Descripción: " + personaje.getDescription());
        layout.add("Género: " + personaje.getGender());
        layout.add("Afiliación: " + personaje.getAffiliation());
        layout.add("Ki: " + personaje.getKi());
        layout.add("Max Ki: " + personaje.getMaxKi());

        Button editar = new Button("Editar", e -> {
            dialog.close();
            mostrarFormulario(personaje);
        });

        Button eliminar = new Button("Eliminar", e -> {
            dialog.close();
            mostrarConfirmacionEliminacion(personaje);
        });

        layout.add(new HorizontalLayout(editar, eliminar));
        dialog.add(layout);
        dialog.open();
    }

    private void mostrarConfirmacionEliminacion(Personaje personaje) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.add("¿Estás seguro de eliminar este personaje?");

        Button aceptar = new Button("Sí", e -> {
            eliminarPersonaje(personaje);
            confirmDialog.close();
        });

        Button cancelar = new Button("No", e -> confirmDialog.close());
        confirmDialog.add(new HorizontalLayout(aceptar, cancelar));
        confirmDialog.open();
    }

    private void mostrarFormulario(Personaje personaje) {
        Dialog dialog = new Dialog();
        FormLayout form = new FormLayout();

        TextField name = new TextField("Nombre");
        TextField description = new TextField("Descripción");
        TextField ki = new TextField("Ki");
        TextField maxKi = new TextField("Max Ki");
        TextField gender = new TextField("Género");
        TextField affiliation = new TextField("Afiliación");
        TextField image = new TextField("Imagen URL");

        if (personaje != null) {
            name.setValue(personaje.getName());
            description.setValue(personaje.getDescription());
            ki.setValue(personaje.getKi());
            maxKi.setValue(personaje.getMaxKi());
            gender.setValue(personaje.getGender());
            affiliation.setValue(personaje.getAffiliation());
            image.setValue(personaje.getImage());
        }

        Button guardar = new Button("Guardar", e -> {
            if (name.isEmpty() || ki.isEmpty()) {
                Notification.show("Nombre y Ki son obligatorios");
                return;
            }

            Personaje nuevo = new Personaje();
            nuevo.setName(name.getValue());
            nuevo.setDescription(description.getValue());
            nuevo.setKi(ki.getValue());
            nuevo.setMaxKi(maxKi.getValue());
            nuevo.setGender(gender.getValue());
            nuevo.setAffiliation(affiliation.getValue());
            nuevo.setImage(image.getValue());

            if (personaje != null) {
                nuevo.setId(personaje.getId());
                nuevo.setDeletedAt(personaje.getDeletedAt());
            }

            guardarPersonaje(nuevo);
            dialog.close();
            cargarPersonajes();
        });

        if (personaje != null && personaje.getDeletedAt() != null) {
            Button resucitar = new Button("Resucitar", e -> {
                personaje.setDeletedAt(null);
                guardarPersonaje(personaje);
                dialog.close();
                cargarPersonajes();
            });
            dialog.add(form, new HorizontalLayout(guardar, resucitar));
        } else {
            dialog.add(form, guardar);
        }

        form.add(name, description, ki, maxKi, gender, affiliation, image);
        dialog.open();
    }

    private void guardarPersonaje(Personaje personaje) {
        try {
            String json = gson.toJson(personaje);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            Notification.show("Error al guardar personaje");
        }
    }

    private void eliminarPersonaje(Personaje personaje) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "/" + personaje.getId()))
                    .DELETE()
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            cargarPersonajes();
        } catch (Exception e) {
            Notification.show("Error al eliminar personaje");
        }
    }

    private void cargarPersonajes() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Type listType = new TypeToken<List<Personaje>>() {}.getType();
            List<Personaje> personajes = gson.fromJson(response.body(), listType);
            grid.setItems(personajes);
        } catch (Exception e) {
            grid.setItems(new ArrayList<>());
            Notification.show("Error al cargar personajes");
        }
    }
}