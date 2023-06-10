package com.isep.dataengineservice.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterApiResponse implements Serializable {
    private List<Tweet> results;
}