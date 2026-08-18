// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testLocaleWithHyphens() throws IOException
{
    assertEquals(new Locale("en", "GB"), MAPPER.readValue(quote("en-GB"), Locale.class));
    assertEquals(new Locale("fr", "FR", "POSIX"),
            MAPPER.readValue(quote("fr-FR-POSIX"), Locale.class));
}