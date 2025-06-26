package org.upm.javier.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Personaje {
    private String name;
    private String description;
    private String ki;
    private String maxKi;
    private String gender;
    private String affiliation;
    private String deletedAt;
    private String id;
    private String image;
}