package com.robotdebris.ncaaps2scheduler.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.robotdebris.ncaaps2scheduler.model.School;

import java.io.IOException;

public class ApiSchoolDeserializer extends StdDeserializer<School> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ApiSchoolDeserializer() {
        this(null);
    }

    public ApiSchoolDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public School deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String name = node.get("school").asText();
        String mascot = node.get("mascot").asText();
        String abbreviation = node.get("abbreviation").asText();
        JsonNode location = node.get("location");
        double latitude = location.path("latitude").asDouble();
        double longitude = location.path("longitude").asDouble();
        String stadiumName = location.path("name").asText();
        String state = location.path("state").asText();
        String city = location.path("city").asText();
        double capacity = location.path("capacity").asDouble();
        String color = node.get("color").asText();
        String altColor = node.get("alt_color").asText();
        // other custom mappings

        School school = new School();
        school.setName(name);
        school.setNickname(mascot);
        school.setAbbreviation(abbreviation);
        school.setLatitude(latitude);
        school.setLongitude(longitude);
        school.setStadiumName(stadiumName);
        school.setState(state);
        school.setCity(city);
        school.setStadiumCapacity(capacity);
        school.setColor(color);
        school.setAltColor(altColor);

        // Assuming 'logos' is an array of strings in your School class
        if (node.has("logos") && node.get("logos").isArray()) {
            JsonNode logosNode = node.get("logos");
            if (logosNode.size() > 0) {
                JsonNode firstLogo = logosNode.get(0);
                if (firstLogo.isTextual()) {
                    String firstLogoUrl = firstLogo.asText();
                    school.setLogo(firstLogoUrl);
                }
            }
        }
        return school;
    }
}