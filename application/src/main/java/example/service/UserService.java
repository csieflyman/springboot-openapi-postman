package example.service;

import base.service.GenericService;
import base.util.query.Query;
import example.model.User;

import java.util.List;

/**
 * @author csieflyman
 */
public interface UserService extends GenericService<User, Long> {

    void create(User user);

    void update(User user);

    void deleteById(Long userId);

    List<User> find(Query query);
}
