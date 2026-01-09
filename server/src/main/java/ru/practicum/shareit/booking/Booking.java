package ru.practicum.shareit.booking;

import jakarta.persistence.*;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", schema = "public")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "start_date")
    private LocalDateTime start;
    @Column(name = "end_date")
    private LocalDateTime end;
    @Column(name = "item_id")
    private Long itemId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private Item item;
    @Column(name = "booker_id")
    private Long bookerId;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
}
