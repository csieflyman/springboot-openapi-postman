package example.service;

import base.service.GenericServiceImpl;
import base.util.query.Junction;
import base.util.query.Query;
import example.dao.UserDao;
import example.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author csieflyman
 */
@Slf4j
@Service("userService")
public class UserServiceImpl extends GenericServiceImpl<User, Long> implements UserService {

    @Autowired
    public UserServiceImpl(@Qualifier("userDao") UserDao userDao) {
        super(userDao);
    }

    @Transactional
    @Override
    public void create(User user) {
        dao.create(user);
    }

    @Transactional
    @Override
    public void update(User user) {
        dao.update(user);
    }

    @Transactional
    @Override
    public void deleteById(Long userId) {
        dao.executeDelete(Junction.and().eq("id", userId));
    }

    @Override
    public List<User> find(Query query) {
        return super.find(query);
    }
}