package example.dao;

import base.dao.AbstractJPADaoImpl;
import example.model.User;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author csieflyman
 */
@Repository("userDao")
public class UserDaoImpl extends AbstractJPADaoImpl<User, Long> implements UserDao {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void init() {
        setEntityManager(em);
    }
}