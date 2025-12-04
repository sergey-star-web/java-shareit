package ru.practicum.shareit.item.dto;

import lombok.Data;

@Data
public class ItemDto {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private Integer request;

    public ItemDto(String name, String description, Boolean available, Integer request) {
        this.name = name;
        this.description = description;
        this.available = available;
        this.request = request;
    }
}