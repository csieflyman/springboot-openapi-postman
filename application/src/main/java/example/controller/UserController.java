package example.controller;

import base.controller.AbstractController;
import base.dto.FormGroups;
import base.util.query.Query;
import example.model.User;
import example.service.UserService;
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

import javax.validation.groups.Default;
import java.util.Optional;

/**
 * @author csieflyman
 */
@Slf4j
@RestController
@RequestMapping(value = "/users")
@ResponseBody
public class UserController extends AbstractController {

    @Autowired
    private UserService userService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Long createUser(@Validated({Default.class, FormGroups.CREATE.class, FormGroups.CREATE_ORDERS.class}) @RequestBody User user, BindingResult result) {
        log.info("================= Create User =========================");
        processBindingResult(result);
        userService.create(user);
        return user.getId();
    }

    @PutMapping(value = {"/{userId}"}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateUser(@PathVariable String userId, @Validated({Default.class, FormGroups.UPDATE.class, FormGroups.UPDATE_ORDERS.class}) @RequestBody User user, BindingResult result) {
        log.info("================= Update User =========================");
        processBindingResult(result);
        user.setId(Long.valueOf(userId));
        userService.update(user);
    }

    @DeleteMapping({"/{userId}"})
    public void deleteUser(@PathVariable String userId) {
        log.info("================= Delete User =========================");
        userService.deleteById(Long.valueOf(userId));
    }

    @ApiResponses(value = { @ApiResponse(code = 200, message = "User Model", response = User.class), @ApiResponse(code = 404, message = "User not found") })
    @GetMapping(value = {"/{userId}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getUser(@PathVariable String userId) {
        log.info("================= Get User =========================");
        Optional<User> result = userService.findById(Long.valueOf(userId));
        return result.isPresent() ? result.get() : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity findUsers(@RequestParam() MultiValueMap<String, String> requestParam) {
        log.info("================= Find User =========================");
        return findEntities(userService, Query.create(requestParam));
    }
}
