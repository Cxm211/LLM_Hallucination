// com/fasterxml/jackson/databind/util/TestTokenBuffer.java
public void testWriteRawValueNull() throws IOException {
    TokenBuffer buf = new TokenBuffer(null, false);
    buf.writeRawValue((String)null);
    JsonParser p = buf.asParser();
    JsonToken t = p.nextToken();
    assert t == JsonToken.VALUE_NULL;
}
