// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testLocaleEmptyStringVariants() throws IOException
{
    // Test empty string with spaces (should trim to empty)
    Locale loc1 = MAPPER.readValue(quote(" "), Locale.class);
    assertSame(Locale.ROOT, loc1);
    
    // Test Locale.ROOT serialization round-trip
    String json = MAPPER.writeValueAsString(Locale.ROOT);
    Locale loc2 = MAPPER.readValue(json, Locale.class);
    assertSame(Locale.ROOT, loc2);
}