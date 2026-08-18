// com/fasterxml/jackson/databind/deser/TestJdkTypes.java
public void testURIEmptyString() throws IOException
{
    // Test empty string URI deserialization
    java.net.URI uri = MAPPER.readValue(quote(""), java.net.URI.class);
    assertNotNull(uri);
    assertEquals("", uri.toString());
    
    // Test URI with actual content still works
    java.net.URI uri2 = MAPPER.readValue(quote("http://example.com"), java.net.URI.class);
    assertEquals("http://example.com", uri2.toString());
}