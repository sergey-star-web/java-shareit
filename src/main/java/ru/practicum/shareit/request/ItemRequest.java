package ru.practicum.shareit.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ItemRequest {
    private Integer id;
    private String description;
    private Integer requestor;
    private LocalDate created;

    public ItemRequest() {

    }
}
