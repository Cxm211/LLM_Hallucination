// com/fasterxml/jackson/databind/ser/jdk/DateSerializationTest.java::testDateISO8601_BCE
public void testDateISO8601_BCE_additionalLargeNegative() throws IOException
    {
        ObjectWriter w = MAPPER.writer()
                .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        serialize(w, judate(-123456, 1, 1,  00, 00, 00, 0, "UTC"),   "-123456-01-01T00:00:00.000+0000");
    }