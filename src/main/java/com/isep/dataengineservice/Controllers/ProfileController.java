package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.User.Posts;
import com.isep.dataengineservice.Models.User.Profile;
import com.isep.dataengineservice.Models.User.User;
import com.isep.dataengineservice.Services.User.ProfileService;
import com.isep.dataengineservice.Services.User.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;

@RestController
public class ProfileController {
    @Autowired
    ProfileService profileService;
    @Autowired
    UserService userService;

    @GetMapping(value="/api/getProfileByUserId")
    public ResponseEntity<Profile> getProfileByUserId(@RequestParam @NotNull Integer userId) throws SQLException {
        Profile profile = profileService.getProfileByUserId(userId);
        User user = userService.getUserById(userId);
        profile.setUser(user);
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @GetMapping(value="/api/getProfileByProfileId")
    public ResponseEntity<Profile> getProfileByProfileId(@RequestParam @NotNull Integer profileId) throws SQLException {
        Profile profile = profileService.getProfileByProfileId(profileId);
        User user = profileService.getUserByProfileId(profile.getProfileId());
        profile.setUser(user);
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @PostMapping(value="/api/addProfile")
    public ResponseEntity<Object> addProfile(@RequestBody @NotNull Profile profile) throws SQLException {
        profileService.addOrUpdateProfile(profile);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value="/api/getMessages")
    public ResponseEntity<List<Posts>> getMessages(@RequestParam @NotNull Integer userId) throws SQLException {
        List<Posts> messages = profileService.getMessagesByUserId(userId);
        return messages != null ? ResponseEntity.ok(messages) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping(value="/api/searchProfiles")
    public ResponseEntity<List<Profile>> searchUsersProfiles(@RequestParam String query) throws SQLException {
        List<Profile> profiles = profileService.searchUsers(query);
        return ResponseEntity.ok(profiles);
    }

}
