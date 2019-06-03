package example.service;

import base.service.GenericServiceImpl;
import base.util.query.Junction;
import base.util.query.Query;
import example.dao.CourseDao;
import example.model.Course;
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
@Service("courseService")
public class CourseServiceImpl extends GenericServiceImpl<Course, Long> implements CourseService {

    @Autowired
    public CourseServiceImpl(@Qualifier("courseDao") CourseDao courseDao) {
        super(courseDao);
    }

    @Transactional
    @Override
    public void create(Course course) {
        dao.create(course);
    }

    @Transactional
    @Override
    public void update(Course course) {
        dao.update(course);
    }

    @Transactional
    @Override
    public void deleteById(Long courseId) {
        dao.executeDelete(Junction.and().eq("id", courseId));
    }

    @Override
    public List<Course> find(Query query) {
        return super.find(query);
    }
}