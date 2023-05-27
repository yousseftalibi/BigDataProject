package com.isep.dataengineservice.Controllers;

import com.isep.dataengineservice.Models.User.ChatMessage;
import com.isep.dataengineservice.Models.User.Profile;
import com.isep.dataengineservice.Services.User.ProfileService;
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

    @GetMapping(value="/api/getProfile")
    public ResponseEntity<Profile> getProfile(@RequestParam @NotNull Integer userId) throws SQLException {
        Profile profile = profileService.getProfileByUserId(userId);
        return profile != null ? ResponseEntity.ok(profile) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    @PostMapping(value="/api/addProfile")
    public ResponseEntity<Object> addProfile(@RequestBody @NotNull Profile profile) throws SQLException {
        profileService.addOrUpdateProfile(profile);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value="/api/getMessages")
    public ResponseEntity<List<ChatMessage>> getMessages(@RequestParam @NotNull Integer userId) throws SQLException {
        List<ChatMessage> messages = profileService.getMessagesByUserId(userId);
        return messages != null ? ResponseEntity.ok(messages) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping(value="/api/searchProfiles")
    public ResponseEntity<List<Profile>> searchUsersProfiles(@RequestParam String query) throws SQLException {
        List<Profile> profiles = profileService.searchUsers(query);
        return ResponseEntity.ok(profiles);
    }

}
