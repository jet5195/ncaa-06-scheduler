package com.robotdebris.ncaaps2scheduler.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.robotdebris.ncaaps2scheduler.model.School;
import com.robotdebris.ncaaps2scheduler.repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SchoolIdDeserializer extends JsonDeserializer<School> {

    @Autowired
    private SchoolRepository schoolRepository;

    @Override
    public School deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        int tgid = jsonParser.getIntValue();
        return schoolRepository.findById(tgid);
    }
}
