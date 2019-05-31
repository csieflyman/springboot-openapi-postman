package base.dto;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author csieflyman
 */
public abstract class DefaultValidator<A extends Annotation, T> implements ConstraintValidator<A, T> {

    protected String messageKey;

    protected Map<String, Object> messageParameters = new HashMap<>();

    public DefaultValidator(String messageKey) {
        this.messageKey = messageKey;
    }

    public abstract boolean isValid(T object);

    @Override
    public boolean isValid(T object, ConstraintValidatorContext constraintContext) {
        boolean isValid = isValid(object);
        if(!isValid) {
            constraintContext.disableDefaultConstraintViolation();
            HibernateConstraintValidatorContext hibernateContext = constraintContext.unwrap(HibernateConstraintValidatorContext.class);
            hibernateContext.withDynamicPayload(object);
            messageParameters.forEach(hibernateContext::addMessageParameter);
            hibernateContext.buildConstraintViolationWithTemplate(messageKey).addConstraintViolation();
        }
        return isValid;
    }
}
