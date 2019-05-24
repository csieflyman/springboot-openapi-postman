package postman.controller;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import postman.User;

/**
 * @author csieflyman
 */
@RestController
@RequestMapping(value = "/users")
@ResponseBody
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public User createUser(@RequestBody User user) {
        logger.info("create user:" + user.getName());
        return user;
    }

    @PutMapping(value = {"/{userId}"}, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateUser(@PathVariable String userId, @RequestBody User user) {
        logger.info("update user:" + user.getName());
    }

    @DeleteMapping({"/{userId}"})
    public void deleteUser(@PathVariable String userId) {
        logger.info("delete user:" + userId);
    }

    @ApiResponses(value = { @ApiResponse(code = 200, message = "User Model", response = User.class), @ApiResponse(code = 404, message = "User not found") })
    @GetMapping(value = {"/{userId}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getUser(@PathVariable String userId) {
        if(userId.equalsIgnoreCase("1"))
            return new User("james", "male", 30);
        else
            return ResponseEntity.notFound().build();
    }
}
