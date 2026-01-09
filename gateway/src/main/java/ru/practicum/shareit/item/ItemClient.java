package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> get(long itemId, long userId) {
        String path = "/" + itemId;
        return get(path, userId);
    }

    public ResponseEntity<Object> getItems(long userId) {
        String path = "";
        return get(path, userId);
    }

    public ResponseEntity<Object> add(long userId, ItemDto item) {
        String path = "";
        return post(path, userId, item);
    }

    public ResponseEntity<Object> update(long userId, long itemId, ItemDto item) {
        String path = "/" + itemId;
        return patch(path, userId, item);
    }

    public ResponseEntity<Object> searchItems(String text, long userId) {
        String path = "/search?text=" + text;
        return get(path, userId);
    }

    public ResponseEntity<Object> addComment(long itemId, CommentDto comment, long userId) {
        String path = "/" + itemId + "/comment";
        return post(path, userId, comment);
    }
}