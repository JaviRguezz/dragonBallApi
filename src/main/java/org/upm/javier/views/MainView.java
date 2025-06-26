package org.upm.javier.views;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
    }

    private HorizontalLayout botonesAcciones(Personaje personaje) {
        Button editar = new Button("Editar", e -> mostrarFormulario(personaje));
        Button borrar = new Button("Eliminar", e -> eliminarPersonaje(personaje));
        return new HorizontalLayout(editar, borrar);
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
            Personaje nuevo = new Personaje();
            nuevo.setName(name.getValue());
            nuevo.setDescription(description.getValue());
            nuevo.setKi(ki.getValue());
            nuevo.setMaxKi(maxKi.getValue());
            nuevo.setGender(gender.getValue());
            nuevo.setAffiliation(affiliation.getValue());
            nuevo.setImage(image.getValue());
            nuevo.setDeletedAt(null);

            if (personaje != null) {
                nuevo.setId(personaje.getId());
            }

            guardarPersonaje(nuevo);
            dialog.close();
            cargarPersonajes();
        });

        form.add(name, description, ki, maxKi, gender, affiliation, image);
        dialog.add(form, guardar);
        dialog.open();
    }

    private void guardarPersonaje(Personaje personaje) {
        try {
            String json = gson.toJson(personaje);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl+"/save"))
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
