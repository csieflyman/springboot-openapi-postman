package example.model;

import base.dto.FormGroups;
import base.model.EntityModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author csieflyman
 */
@Getter
@Setter
@Entity
public class User extends EntityModel<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Size(min = 1, max = 20)
    @NotNull(groups = FormGroups.CREATE.class)
    @Column
    private String name;

    @NotNull(groups = FormGroups.CREATE.class)
    @Column
    private Gender gender;

    @Min(value = 0)
    @NotNull(groups = FormGroups.CREATE.class)
    @Column
    private Integer age;

    @NotNull(groups = FormGroups.CREATE.class)
    @Column
    private Boolean isMember;
}
