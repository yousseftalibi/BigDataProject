package com.isep.dataengineservice.Repository.User;

import com.isep.dataengineservice.Models.User.Posts;
import com.isep.dataengineservice.Models.User.Profile;
import com.isep.dataengineservice.Models.User.User;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProfileRepository {
    @Autowired
    Connection connection;

    public Profile getProfileByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM profile WHERE user_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, userId);
        var result = ps.executeQuery();
        RowMapper<Profile> rowMapper = new BeanPropertyRowMapper<>(Profile.class);
        return result.next() ? rowMapper.mapRow(result, 0) : null;
    }

    public Profile getProfileByProfileId(int profileId) throws SQLException {
        String query = "SELECT * FROM profile WHERE profile_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, profileId);
        var result = ps.executeQuery();
        RowMapper<Profile> rowMapper = new BeanPropertyRowMapper<>(Profile.class);
        return result.next() ? rowMapper.mapRow(result, 0) : null;
    }
    public void addOrUpdateProfile(Profile profile) throws SQLException {
        // first try to update existing profile
        String updateQuery = "UPDATE profile SET first_name = ?, last_name = ?, email_address = ?, nationality = ? WHERE user_id = ?";
        PreparedStatement updatePs = connection.prepareStatement(updateQuery);
        updatePs.setString(1, profile.getFirstName());
        updatePs.setString(2, profile.getLastName());
        updatePs.setString(3, profile.getEmailAddress());
        updatePs.setString(4, profile.getNationality());
        updatePs.setInt(5, profile.getUser().getId());
        int affectedRows = updatePs.executeUpdate();

        // if no rows were affected, the profile doesn't exist, so we try to insert
        if (affectedRows == 0) {
            String insertQuery = "INSERT INTO profile (user_id, first_name, last_name, email_address, nationality) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertPs = connection.prepareStatement(insertQuery);
            insertPs.setInt(1, profile.getUser().getId());
            insertPs.setString(2, profile.getFirstName());
            insertPs.setString(3, profile.getLastName());
            insertPs.setString(4, profile.getEmailAddress());
            insertPs.setString(5, profile.getNationality());
            insertPs.executeUpdate();
        }
    }

    public void saveMessage(Posts posts) {
        try {
            String insertQuery = "INSERT INTO messages (user_id, message) VALUES (?, ?)";
            PreparedStatement insertPs = connection.prepareStatement(insertQuery);
            insertPs.setInt(1, posts.getId() );
            insertPs.setString(2, posts.getMessage());
            insertPs.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Posts> getMessagesByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM messages WHERE user_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, userId);
        var result = ps.executeQuery();
        RowMapper<Posts> rowMapper = new BeanPropertyRowMapper<>(Posts.class);
        List<Posts> messages = new ArrayList<>();
        while (result.next()) {
            messages.add(rowMapper.mapRow(result, 0));
        }
        return messages;
    }

    public List<Profile> searchUsers(String query) throws SQLException {
        String sql = "SELECT * FROM profile WHERE first_name LIKE ? OR last_name LIKE ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, "%" + query + "%");
        ps.setString(2, "%" + query + "%");
        ResultSet rs = ps.executeQuery();
        RowMapper<Profile> rowMapper = new BeanPropertyRowMapper<>(Profile.class);
        List<Profile> profiles = new ArrayList<>();
        while (rs.next()) {
            profiles.add(rowMapper.mapRow(rs, 0));
        }
        return profiles;
    }

    public User getUserByProfileId(int profileId) throws SQLException {
        String query = "SELECT u.* FROM users u JOIN profile p ON u.id = p.user_id WHERE p.profile_id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, profileId);
        var result = ps.executeQuery();
        RowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);
        return result.next() ? rowMapper.mapRow(result, 0) : null;
    }
}
