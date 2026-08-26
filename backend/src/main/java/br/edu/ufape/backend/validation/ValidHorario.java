package br.edu.ufape.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidHorarioValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHorario {
    String message() default "Horário de fim deve ser maior que horário de início";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}