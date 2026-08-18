// com/fasterxml/jackson/databind/util/TestTokenBuffer.java
public void testTokensWithoutValue() throws IOException
{
    TokenBuffer buf = new TokenBuffer(null, false);
    StringWriter w = new StringWriter();
    JsonGenerator gen = MAPPER.getFactory().createGenerator(w);

    // Test tokens without values: START_ARRAY, END_ARRAY, START_OBJECT, END_OBJECT
    buf.writeStartArray();
    gen.writeStartArray();
    _verifyOutputContext(buf, gen);

    buf.writeBoolean(true);
    gen.writeBoolean(true);
    _verifyOutputContext(buf, gen);

    buf.writeBoolean(false);
    gen.writeBoolean(false);
    _verifyOutputContext(buf, gen);

    buf.writeNull();
    gen.writeNull();
    _verifyOutputContext(buf, gen);

    buf.writeEndArray();
    gen.writeEndArray();
    _verifyOutputContext(buf, gen);

    buf.close();
    gen.close();
}