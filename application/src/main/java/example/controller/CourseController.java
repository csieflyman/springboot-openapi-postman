package example.controller;

import base.controller.AbstractController;
import base.dto.FormGroups;
import base.util.query.Query;
import example.model.Course;
import example.service.CourseService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author csieflyman
 */
@Slf4j
@RestController
@RequestMapping(value = "/courses")
@ResponseBody
public class CourseController extends AbstractController {

    @Autowired
    private CourseService courseService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Long createCourse(@Validated({FormGroups.CREATE.class}) @RequestBody Course course, BindingResult result) {
        log.info("================= Create Course =========================");
        processBindingResult(result);
        courseService.create(course);
        return course.getId();
    }

    @PutMapping(value = {"/{courseId}"}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateCourse(@PathVariable String courseId, @Validated({FormGroups.UPDATE.class}) @RequestBody Course course, BindingResult result) {
        log.info("================= Update Course =========================");
        processBindingResult(result);
        course.setId(Long.valueOf(courseId));
        courseService.update(course);
    }

    @DeleteMapping({"/{courseId}"})
    public void deleteCourse(@PathVariable String courseId) {
        log.info("================= Delete Course =========================");
        courseService.deleteById(Long.valueOf(courseId));
    }

    @ApiResponses(value = { @ApiResponse(code = 200, message = "Course Model", response = Course.class), @ApiResponse(code = 404, message = "Course not found") })
    @GetMapping(value = {"/{courseId}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getCourse(@PathVariable String courseId) {
        log.info("================= Get Course =========================");
        Optional<Course> result = courseService.findById(Long.valueOf(courseId));
        return result.isPresent() ? result.get() : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity findCourses(@RequestParam() MultiValueMap<String, String> requestParam) {
        log.info("================= Find Course =========================");
        return findEntities(courseService, Query.create(requestParam));
    }
}
