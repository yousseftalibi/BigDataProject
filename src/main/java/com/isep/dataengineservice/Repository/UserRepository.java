package com.isep.dataengineservice.Repository;

import com.isep.dataengineservice.Models.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import lombok.var;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    Connection connection;


    public Optional<User> getUserById(int id) throws SQLException{
        String query = "SELECT * FROM users WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, id);
        var result = ps.executeQuery();
        RowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);
        User user = result.next() ? rowMapper.mapRow(result, 0) : null;
        return Optional.of(user);
    }

    public List<Integer> getUserFriends(@NotNull User user) throws SQLException {
        String friendsQuery = "SELECT friend_id FROM friends WHERE user_id = ? ";
        PreparedStatement ps = connection.prepareStatement(friendsQuery);
        ps.setInt(1, user.getId());
        var result = ps.executeQuery();
        List<Integer> ids = new ArrayList<>();
        while (result.next()) ids.add(result.getInt("friend_id"));
        System.out.println("User friends are : ");
        ids.forEach(System.out::println);
        return ids;
    }
    public Optional<User> getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        PreparedStatement ps = connection.prepareStatement(query);
        ps.setString(1, username);
        try {
            var result = ps.executeQuery();
            RowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);
            var user = result.next() ? rowMapper.mapRow(result, 0) : null;
            return Optional.ofNullable(user);

        }
        catch (SQLException sqlE){
            throw new RuntimeException(sqlE);
        }

    }
    public void registerUser(User user) throws SQLException {
        String createUser = "INSERT INTO users (username, password) values (?, ?)";
        PreparedStatement ps = connection.prepareStatement(createUser);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getPassword());
        ps.executeUpdate();
    }





}
