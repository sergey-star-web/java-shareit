package ru.practicum.shareit.item.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE_USE, METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = UserRentedItemValidator.class)
public @interface UserRentedItemValid {
    String message() default "User has not rented the item.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}