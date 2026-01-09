package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemsRequestDto {
    private Long id;
    private String description;
    private LocalDate created;
    private List<ItemForRequestDto> items = new ArrayList<>();
}
