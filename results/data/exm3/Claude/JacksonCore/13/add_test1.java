// com/fasterxml/jackson/core/json/TestJsonGeneratorFeatures.java
public void testFieldNameQuotingToggle() throws IOException
{
    JsonFactory jf = new JsonFactory();
    
    StringWriter sw = new StringWriter();
    JsonGenerator gen = jf.createGenerator(sw);
    gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
    gen.writeStartObject();
    gen.writeFieldName("first");
    gen.writeNumber(1);
    gen.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
    gen.writeFieldName("second");
    gen.writeNumber(2);
    gen.writeEndObject();
    gen.close();
    
    assertEquals("{first:1,\"second\":2}", sw.toString());
}