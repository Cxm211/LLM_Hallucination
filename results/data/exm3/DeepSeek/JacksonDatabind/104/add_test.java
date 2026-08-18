// com/fasterxml/jackson/databind/ser/jdk/DateSerializationTest.java
public void testDateISO8601_Additional() throws IOException
{
    ObjectWriter w = MAPPER.writer()
            .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // Positive year within 1-9999
    serialize(w, judate(2024, 1, 1, 00, 00, 00, 0, "UTC"), "+2024-01-01T00:00:00.000+0000");
    // Positive year exactly 10000
    serialize(w, judate(10000, 1, 1, 00, 00, 00, 0, "UTC"), "+10000-01-01T00:00:00.000+0000");
    // Negative year with magnitude <10000
    serialize(w, judate(-9999, 1, 1, 00, 00, 00, 0, "UTC"), "-9999-01-01T00:00:00.000+0000");
    // Negative year with magnitude >=10000
    serialize(w, judate(-10000, 1, 1, 00, 00, 00, 0, "UTC"), "-10000-01-01T00:00:00.000+0000");
    // Non-zero offset, positive
    serialize(w, judate(2024, 1, 1, 00, 00, 00, 0, "GMT+05:30"), "+2024-01-01T00:00:00.000+0530");
    // Non-zero offset, negative
    serialize(w, judate(2024, 1, 1, 00, 00, 00, 0, "GMT-08:00"), "+2024-01-01T00:00:00.000-0800");
}
