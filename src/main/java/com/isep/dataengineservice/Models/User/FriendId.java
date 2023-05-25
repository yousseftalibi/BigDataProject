package com.isep.dataengineservice.Models.User;

import lombok.Data;

import java.io.Serializable;

@Data
public class FriendId implements Serializable {
    private int userId;
    private int friendId;

    public FriendId() {}

    public FriendId(int userId, int friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }
}

