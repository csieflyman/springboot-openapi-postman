package base.dto;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author csieflyman
 */
public class MyConstraints {

    @Target({FIELD})
    @Retention(RUNTIME)
    @Constraint(validatedBy = EnumValidator.class)
    public @interface Enumerated {
        String message() default EnumValidator.messageKey;
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
        String[] stringValues() default {};
        int[] intValues() default {};
        boolean ignoreCase() default false;
    }

    public static class EnumValidator extends DefaultValidator<Enumerated, Object> {

        final static public String messageKey = "base.enum";

        private String[] stringValues;
        private int[] intValues;
        private boolean ignoreCase;

        public EnumValidator() {
            super(messageKey);
        }

        @Override
        public void initialize(Enumerated constraintAnnotation) {
            this.stringValues = constraintAnnotation.stringValues();
            this.intValues = constraintAnnotation.intValues();
            this.ignoreCase = constraintAnnotation.ignoreCase();
        }

        @Override
        public boolean isValid(Object input) {
            if(input == null)
                return true;

            if(stringValues != null && stringValues.length > 0) {
                for(String validString: stringValues) {
                    if(ignoreCase && validString.equalsIgnoreCase((String)input))
                        return true;
                    else if(!ignoreCase && validString.equals(input))
                        return true;
                }
            }
            else if(intValues != null && intValues.length > 0) {
                for(int validInteger: intValues) {
                    if(validInteger == (Integer)input)
                        return true;
                }
            }
            String enumValues = getEnumValueString();
            messageParameters.put("enumValues", enumValues);
            return false;
        }

        private String getEnumValueString() {
            String joinedValueString = "";
            if(stringValues != null) {
                joinedValueString = Arrays.toString(stringValues);
            }
            else if(intValues != null) {
                joinedValueString = Arrays.toString(intValues);
            }
            return joinedValueString;
        }
    }
}
