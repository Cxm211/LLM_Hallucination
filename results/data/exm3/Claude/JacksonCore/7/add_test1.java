// com/fasterxml/jackson/core/json/GeneratorFailTest.java
public void testFailOnWritingStringInArrayContext() throws Exception {
        // Test writing values in array context
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonFactory().createGenerator(bytes);
        gen.writeStartArray();
        gen.writeNumber(1);
        try {
            gen.writeFieldName("invalid");
            fail("Should not allow field name in array context");
        } catch (JsonGenerationException e) {
            verifyException(e, "Can not write a field name");
        }
        gen.close();
    }