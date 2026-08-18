// com/fasterxml/jackson/core/json/GeneratorFailTest.java
public void testFailOnWritingStringInRootContext() throws Exception {
        // Test writing multiple values in root context to trigger the space logic
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonFactory().createGenerator(bytes);
        gen.writeNumber(1);
        try {
            gen.writeFieldName("invalid");
            fail("Should not allow field name in root context");
        } catch (JsonGenerationException e) {
            verifyException(e, "Can not write a field name");
        }
        gen.close();
    }