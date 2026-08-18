// com/fasterxml/jackson/core/json/RawValueWithSurrogatesTest.java
public void testRawWithSurrogatesAtBufferBoundary() throws Exception {
    // Test with surrogate pair split at buffer boundary
    StringBuilder sb = new StringBuilder();
    // Create a string that would cause buffer boundary split
    // Assuming buffer size is around 500-2000 chars, create string close to that
    for (int i = 0; i < 200; i++) {
        sb.append("test");
    }
    // Add surrogate pair near the end
    sb.append("\uD834\uDD1E"); // Musical symbol
    sb.append("end");
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator gen = new JsonFactory().createGenerator(out);
    gen.writeRaw(sb.toString());
    gen.flush();
    gen.close();
    
    String result = out.toString("UTF-8");
    assertTrue(result.contains("\uD834\uDD1E"));
    assertTrue(result.endsWith("end"));
}