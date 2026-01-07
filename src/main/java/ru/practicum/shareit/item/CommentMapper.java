package ru.practicum.shareit.item;

import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

@NoArgsConstructor
public class CommentMapper {
    public static Comment toComment(CommentDto commentDto, Long userId) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setCreated(commentDto.getCreated());
        comment.setAuthorId(userId);
        comment.setItem(commentDto.getItem());
        return comment;
    }
}