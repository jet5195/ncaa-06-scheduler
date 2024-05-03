package com.robotdebris.ncaaps2scheduler.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.robotdebris.ncaaps2scheduler.model.School;

import java.io.IOException;
import java.util.List;

public class SchoolIdSerializer extends StdSerializer<Object> {

    public SchoolIdSerializer() {
        this(null);
    }

    public SchoolIdSerializer(Class<Object> t) {
        super(t);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value instanceof School school) {
            gen.writeNumber(school.getTgid());
        } else if (value instanceof List<?> list) {
            // Handle the list of School objects
            gen.writeStartArray();
            for (Object item : list) {
                if (item instanceof School school) {
                    gen.writeNumber(school.getTgid());
                }
            }
            gen.writeEndArray();
        }
    }
}