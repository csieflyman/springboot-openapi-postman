package base.model;

import base.util.DateTimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.Instant;

/**
 * @author csieflyman
 */
@Setter
@Getter
@MappedSuperclass
public abstract class EntityModel<ID> extends BaseModel<ID> {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN, timezone = DateTimeUtils.UTC_ZONE)
    @Column(updatable = false, insertable = false, columnDefinition = "timestamp NULL DEFAULT CURRENT_TIMESTAMP")
    protected Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimeUtils.DATE_TIME_PATTERN, timezone = DateTimeUtils.UTC_ZONE)
    @Column(updatable = false, insertable = false, columnDefinition = "timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    protected Instant updatedAt;
}
