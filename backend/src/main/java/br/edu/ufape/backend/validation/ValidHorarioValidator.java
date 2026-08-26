package br.edu.ufape.backend.validation;

import br.edu.ufape.backend.dto.AvailabilityRequest;
import br.edu.ufape.backend.dto.ReservationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidHorarioValidator implements ConstraintValidator<ValidHorario, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        if (obj instanceof ReservationRequest r) {
            if (r.getHorarioInicio() == null || r.getHorarioFim() == null) return false;
            return r.getHorarioFim().isAfter(r.getHorarioInicio());
        }
        if (obj instanceof AvailabilityRequest a) {
            if (a.getHorarioInicio() == null || a.getHorarioFim() == null) return false;
            return a.getHorarioFim().isAfter(a.getHorarioInicio());
        }
        return true;
    }
}