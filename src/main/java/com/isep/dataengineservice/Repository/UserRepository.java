package com.isep.dataengineservice.Repository;

import com.isep.dataengineservice.Models.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import lombok.var;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    Connection connection;


    public User getUserById(int id) throws SQLException{
        String query = "SELECT * FROM users WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        var result = ps.executeQuery();
        RowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);
        User user = result.next() ? rowMapper.mapRow(result, 0) : null;
        return user;
    }

    public List<Integer> getUserFriendIds(@NotNull User user) throws SQLException {
        String friendsQuery = "SELECT friend_id FROM friends WHERE user_id = ? ";
        PreparedStatement ps = connection.prepareStatement(friendsQuery);
        ps.setInt(1, user.getId());
        var result = ps.executeQuery();
        List<Integer> ids = new ArrayList<>();
        while (result.next()) ids.add(result.getInt("friend_id"));
        return ids;
    }

    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, username);
        try {
            var result = ps.executeQuery();
            RowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);
            var user = result.next() ? rowMapper.mapRow(result, 0) : null;
            return user;

        }
        catch (SQLException sqlE){
            throw new SQLException(sqlE);
        }

    }
    public void registerUser(User user) throws SQLException {
        String createUser = "INSERT INTO users (username, password) values (?, ?)";
        PreparedStatement ps = connection.prepareStatement(createUser);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getPassword());
        ps.executeUpdate();
    }

    public Array getVisitedPlaces (User user) throws SQLException{
        String getVisitedPlaces = "SELECT places FROM users where id = ?";
        PreparedStatement ps = connection.prepareStatement(getVisitedPlaces);
        ps.setInt(1, user.getId());
        var result = ps.executeQuery();
        return result.next() ? result.getArray("places") : null;
    }






}
