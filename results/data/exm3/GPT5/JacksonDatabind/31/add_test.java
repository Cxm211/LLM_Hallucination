// com/fasterxml/jackson/databind/util/TestTokenBuffer.java::testOutputContext
public void testOutputContextValueWrite() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        StringWriter w = new StringWriter();
        JsonGenerator gen = MAPPER.getFactory().createGenerator(w);

        buf.writeStartObject();
        gen.writeStartObject();
        _verifyOutputContext(buf, gen);

        buf.writeFieldName("x");
        gen.writeFieldName("x");
        _verifyOutputContext(buf, gen);

        buf.writeNumber(1);
        gen.writeNumber(1);
        _verifyOutputContext(buf, gen);

        buf.writeEndObject();
        gen.writeEndObject();
        _verifyOutputContext(buf, gen);

        buf.close();
        gen.close();
    }