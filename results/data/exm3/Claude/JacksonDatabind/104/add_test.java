// com/fasterxml/jackson/databind/ser/jdk/DateSerializationTest.java
public void testDateISO8601_EdgeCases() throws IOException
{
    ObjectWriter w = MAPPER.writer()
            .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    // Test year 9999 (boundary of 4-digit range)
    serialize(w, judate(9999, 12, 31, 23, 59, 59, 999, "UTC"), "9999-12-31T23:59:59.999+0000");
    
    // Test year 10000 (just beyond 4-digit range, needs plus prefix)
    serialize(w, judate(10000, 1, 1, 0, 0, 0, 0, "UTC"), "+10000-01-01T00:00:00.000+0000");
    
    // Test negative year -100
    serialize(w, judate(-100, 6, 15, 12, 30, 45, 500, "UTC"), "-0100-06-15T12:30:45.500+0000");
    
    // Test year 1 (minimal positive 4-digit case)
    serialize(w, judate(1, 1, 1, 0, 0, 0, 0, "UTC"), "0001-01-01T00:00:00.000+0000");
}