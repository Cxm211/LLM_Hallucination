// com/fasterxml/jackson/core/json/RawValueWithSurrogatesTest.java
public void testRawWithLongStringAndSurrogates() throws Exception {
    // Test with very long string containing surrogates to trigger segmentation
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 500; i++) {
        sb.append("x");
        if (i % 100 == 99) {
            sb.append("\uD834\uDD1E"); // Add surrogate pair periodically
        }
    }
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator gen = new JsonFactory().createGenerator(out);
    gen.writeRaw(sb.toString());
    gen.flush();
    gen.close();
    
    String result = out.toString("UTF-8");
    assertEquals(sb.toString(), result);
}