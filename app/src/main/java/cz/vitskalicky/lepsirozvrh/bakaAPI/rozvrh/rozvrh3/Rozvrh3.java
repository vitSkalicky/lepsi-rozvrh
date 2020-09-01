package cz.vitskalicky.lepsirozvrh.bakaAPI.rozvrh.rozvrh3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Rozvrh of the Bakaláři API v3
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rozvrh3 {
    Hour3[] hours;
    Day3[] days;
    Class3[] classes;
    Group3[] groups;
    Subject3[] subjects;
    Teacher3[] teachers;
    Room3[] rooms;
    Cycle3[] cycles;
}
