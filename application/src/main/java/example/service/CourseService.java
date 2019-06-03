package example.service;

import base.service.GenericService;
import base.util.query.Query;
import example.model.Course;

import java.util.List;

/**
 * @author csieflyman
 */
public interface CourseService extends GenericService<Course, Long> {

    void create(Course course);

    void update(Course course);

    void deleteById(Long courseId);

    List<Course> find(Query query);
}
