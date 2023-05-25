package com.isep.dataengineservice.Repository;

import com.isep.dataengineservice.Models.Trip.Place;
import com.isep.dataengineservice.Models.User.User;
import com.isep.dataengineservice.Services.User.UserService;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class TripRepository {
    @Autowired
    UserService userService;
    @Autowired
    Connection connection;

    public Array getVisitedPlaces (User user) throws SQLException{
        String getVisitedPlaces = "SELECT places FROM users where id = ?";
        PreparedStatement ps = connection.prepareStatement(getVisitedPlaces);
        ps.setInt(1, user.getId());
        var result = ps.executeQuery();
        return result.next() ? result.getArray("places") : null;
    }
    public void addVisitedToUser(Integer userId, Place visitedNewPlace) throws SQLException {
        User user = userService.getUserById(userId);
        Array sqlArray = getVisitedPlaces(user);
        List<String> visitedPlaces = new ArrayList<>();
        if(sqlArray != null){
            String[] visitedPlacesArray = (String[]) sqlArray.getArray();
            visitedPlaces = new ArrayList<>(Arrays.asList(visitedPlacesArray));

        }
        if(!visitedPlaces.contains(visitedNewPlace.getXid())){
            visitedPlaces.add(visitedNewPlace.getXid());
            String addVisitedPlaceToUser = "UPDATE users SET places = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(addVisitedPlaceToUser);
            Array updatedPlaces = connection.createArrayOf("text", visitedPlaces.toArray());
            ps.setArray(1, updatedPlaces);
            ps.setInt(2, user.getId());
            ps.executeUpdate();
        }
    }

      public Place getPlaceById(String xid) throws SQLException {
        String getPlaceByIdQuery = "SELECT * FROM places WHERE xid = ?";
        PreparedStatement ps = connection.prepareStatement(getPlaceByIdQuery);
        ps.setString(1, xid);
        var result = ps.executeQuery();
        if(result.next()){
            return Place.builder().xid(result.getString("xid")).rate(result.getInt("rate")).kinds(result.getString("kinds")).dist(result.getDouble("dist")).name(result.getString("name")).build();
        }   
        return null;
    }

    public Boolean placeAlreadyExists (Place place) throws SQLException {
        String placeExistsQuery = "SELECT * FROM places WHERE xid = ?";
        PreparedStatement ps = connection.prepareStatement(placeExistsQuery);
        ps.setString(1, place.getXid());
        var result = ps.executeQuery();
       return result.next();
    }
    
    public void addPlaceToVisitedPlaces (Place place) throws SQLException {
        String visitPlaceQuery = "INSERT INTO places (xid, rate, kinds, dist, name) values (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(visitPlaceQuery);
        ps.setString(1, place.getXid());
        ps.setInt(2, place.getRate());
        ps.setString(3, place.getKinds());
        ps.setDouble(4, place.getDist());
        ps.setString(5, place.getName());
        ps.executeUpdate();
    }



}
