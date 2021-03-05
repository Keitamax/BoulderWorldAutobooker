package com.max.autobooker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Maxime Rocchia
 */
@Getter @Setter
public class Climber {
    @JsonProperty
    private String name;

    @JsonProperty
    private String email;

    @JsonProperty
    private String phone;
}
