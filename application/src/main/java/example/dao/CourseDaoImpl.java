package example.dao;

import base.dao.AbstractJPADaoImpl;
import example.model.Course;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author csieflyman
 */
@Repository("courseDao")
public class CourseDaoImpl extends AbstractJPADaoImpl<Course, Long> implements CourseDao {

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    public void init() {
        setEntityManager(em);
    }
}