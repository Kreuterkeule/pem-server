package com.kreuterkeule.PEM.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreatePDto {

    private String name;
    private String description;
    private String client;
    private List<String> responsible;
    private String deadline;

}
