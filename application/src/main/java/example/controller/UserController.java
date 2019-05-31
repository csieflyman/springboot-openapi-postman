package example.controller;

import base.controller.AbstractController;
import base.dto.CreateFormGroups;
import base.dto.UpdateFormGroups;
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
    public Long createUser(@Validated({CreateFormGroups.class}) @RequestBody User user, BindingResult result) {
        processBindingResult(result);
        userService.create(user);
        return user.getId();
    }

    @PutMapping(value = {"/{userId}"}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateUser(@PathVariable String userId, @Validated({UpdateFormGroups.class}) @RequestBody User user, BindingResult result) {
        processBindingResult(result);
        user.setId(Long.valueOf(userId));
        userService.update(user);
    }

    @DeleteMapping({"/{userId}"})
    public void deleteUser(@PathVariable String userId) {
        userService.deleteById(Long.valueOf(userId));
    }

    @ApiResponses(value = { @ApiResponse(code = 200, message = "User Model", response = User.class), @ApiResponse(code = 404, message = "User not found") })
    @GetMapping(value = {"/{userId}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getUser(@PathVariable String userId) {
        Optional<User> result = userService.findById(Long.valueOf(userId));
        return result.isPresent() ? result.get() : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity findUsers(@RequestParam() MultiValueMap<String, String> requestParam) {
        return findEntities(userService, Query.create(requestParam));
    }
}
