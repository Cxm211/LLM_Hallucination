// com/fasterxml/jackson/core/json/RawValueWithSurrogatesTest.java
public void testRawWithMultipleSurrogatePairs() throws Exception {
    // Test with multiple surrogate pairs
    String input = "start\uD834\uDD1Emiddle\uD83D\uDE00end\uD800\uDC00";
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator gen = new JsonFactory().createGenerator(out);
    gen.writeRaw(input);
    gen.flush();
    gen.close();
    
    String result = out.toString("UTF-8");
    assertEquals(input, result);
}