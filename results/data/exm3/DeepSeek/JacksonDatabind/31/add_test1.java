// com/fasterxml/jackson/databind/util/TestTokenBuffer.java
public void testWriteRawValueCharArray() throws IOException {
    TokenBuffer buf = new TokenBuffer(null, false);
    char[] chars = {'a', 'b', 'c'};
    buf.writeRawValue(chars, 0, 3);
    JsonParser p = buf.asParser();
    JsonToken t = p.nextToken();
    assert t == JsonToken.VALUE_EMBEDDED_OBJECT;
    Object val = p.getEmbeddedObject();
    assert val instanceof RawValue;
}
