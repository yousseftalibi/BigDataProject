package com.isep.dataengineservice.Repository.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class FriendRepository {

    @Autowired
    Connection connection;

    public void addFriend(int userId, int friendId) throws SQLException {
        String insertQuery = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(insertQuery);
        ps.setInt(1, userId);
        ps.setInt(2, friendId);
        ps.executeUpdate();
    }
}