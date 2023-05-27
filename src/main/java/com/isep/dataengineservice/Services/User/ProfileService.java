package com.isep.dataengineservice.Services.User;

import com.isep.dataengineservice.Models.User.ChatMessage;
import com.isep.dataengineservice.Models.User.Profile;
import com.isep.dataengineservice.Repository.User.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.List;

@Service
@Transactional
public class ProfileService {
    @Autowired
    ProfileRepository profileRepository;

    public Profile getProfileByUserId(int userId) throws SQLException {
        return profileRepository.getProfileByUserId(userId);
    }

    public void addOrUpdateProfile(Profile profile) throws SQLException {
        profileRepository.addOrUpdateProfile(profile);
    }
    public List<ChatMessage> getMessagesByUserId(int userId) throws SQLException {
        return profileRepository.getMessagesByUserId(userId);
    }
    public List<Profile> searchUsers(String query) throws SQLException {
        return profileRepository.searchUsers(query);
    }
}
