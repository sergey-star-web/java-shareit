package ru.practicum.shareit.item.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.ItemService;

@RequiredArgsConstructor
public class UserRentedItemValidator implements ConstraintValidator<UserRentedItemValid, Object> {
    private final ItemService itemService;

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Пример: предположим, что value содержит userId и itemId
        Long userId = (Long) ((Object[]) value)[0];
        Long itemId = (Long) ((Object[]) value)[1];

        if (!itemService.checkIfUserRentedItem(userId, itemId)) {
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
