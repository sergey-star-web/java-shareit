package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments", schema = "public")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String text;
    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    @Column(name = "create_date")
    private LocalDateTime created;
}
