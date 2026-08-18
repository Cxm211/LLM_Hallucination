// com/fasterxml/jackson/core/json/TestJsonGeneratorFeatures.java
public void testFieldNameQuotingDisabled() throws IOException
{
    JsonFactory jf = new JsonFactory();
    jf.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
    
    StringWriter sw = new StringWriter();
    JsonGenerator gen = jf.createGenerator(sw);
    gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
    gen.writeStartObject();
    gen.writeFieldName("bar");
    gen.writeNumber(2);
    gen.writeEndObject();
    gen.close();
    
    assertEquals("{bar:2}", sw.toString());
}