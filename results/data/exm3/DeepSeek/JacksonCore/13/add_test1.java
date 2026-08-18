// com/fasterxml/jackson/core/json/TestJsonGeneratorFeatures.java
public void testDisableQuoteFieldNamesGenerator() throws IOException {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = factory.createGenerator(sw);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        gen.writeStartObject();
        gen.writeFieldName("foo");
        gen.writeNumber(1);
        gen.writeEndObject();
        gen.close();
        assertEquals("{foo:1}", sw.toString());
    }
