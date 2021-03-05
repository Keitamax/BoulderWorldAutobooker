package com.max.autobooker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Collection;

/**
 * @author Maxime Rocchia
 */
@Getter @Setter
public class BookingInfo {

    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate date;

    @JsonProperty
    private String timeslot;

    @JsonProperty
    private String pass;

    @JsonProperty
    private String passHolder;

    @JsonProperty
    private Collection<Climber> climbers;
}
