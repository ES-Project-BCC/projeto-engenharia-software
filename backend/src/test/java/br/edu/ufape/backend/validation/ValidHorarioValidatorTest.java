package br.edu.ufape.backend.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidHorarioValidatorTest {
    ValidHorarioValidator validHorarioValidator = new ValidHorarioValidator();

    @Test
    void testIsValid() {
        boolean result = validHorarioValidator.isValid("obj", null);
        Assertions.assertEquals(true, result);

    }
}
