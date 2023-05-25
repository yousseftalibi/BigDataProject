package com.isep.dataengineservice.Models.User;

import javax.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "friends")
@IdClass(FriendId.class)
public class Friend {
    @Id
    private int userId;
    @Id
    private int friendId;

}
