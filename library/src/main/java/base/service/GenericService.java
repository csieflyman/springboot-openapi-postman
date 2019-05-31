package base.service;

import base.model.BaseModel;
import base.util.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * @author csieflyman
 */
public interface GenericService<T extends BaseModel<ID>, ID> {

    T getById(ID id);

    Optional<T> findById(ID id);

    Optional<T> findOne(Query query);

    List<T> find(Query query);

    long findSize(Query query);
}
