package com.isep.dataengineservice.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class DbConfig {
    @Bean
    public Connection connection() throws SQLException {

        String url = "jdbc:postgresql://dpg-cglesuu4dad69r7upa9g-a.frankfurt-postgres.render.com:5432/tripy";
        String user = "admin";
        String pass = "RqrWKnc3gCJkTOnhO4PAk9Rt6BqQjdXV";

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(pass);

        return dataSource.getConnection();
    }
}
