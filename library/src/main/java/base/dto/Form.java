package base.dto;

import base.exception.InvalidEntityException;
import base.util.BeanUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.ParameterizedType;
import java.util.Date;

/**
 * @author csieflyman
 */
@ToString
public abstract class Form<T> {

    @Getter
    @JsonIgnore
    private Date requestTime = new Date();

    @Getter
    @Setter
    @JsonIgnore
    private String body;

    @JsonIgnore
    protected final Class<T> clazz;

    public Form() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public T toModel() {
        Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        T instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidEntityException(String.format("fail to initialize instance %s", clazz.getName()), e);
        }
        BeanUtils.copyIgnoreNull(this, instance);
        return instance;
    }
}
