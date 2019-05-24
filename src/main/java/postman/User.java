package postman;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

/**
 * @author csieflyman
 */
@Data
public class User {

    @NonNull
    @NotNull
    private String name;
    @NonNull
    @NotNull
    private String gender;
    @NonNull
    @NotNull
    private Integer age;
}
