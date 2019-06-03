package example.model;

import base.dto.FormGroups;
import base.model.EntityModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author csieflyman
 */
@Getter
@Setter
@Entity
public class Course extends EntityModel<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column
    private Long id;

    @Size(min = 1, max = 20)
    @NotBlank(groups = FormGroups.CREATE.class)
    @Column
    private String name;
}
