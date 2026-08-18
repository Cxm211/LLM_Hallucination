protected final String[] _deserializeCustom(JsonParser jp, DeserializationContext ctxt) throws IOException
{
    final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
    Object[] chunk = buffer.resetAndStart();
    final JsonDeserializer<String> deser = _elementDeserializer;
    
    int ix = 0;
    try {
        JsonToken t;
        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            String value = (t == JsonToken.VALUE_NULL) ? deser.getNullValue() : deser.deserialize(jp, ctxt);
            if (ix >= chunk.length) {
                chunk = buffer.appendCompletedChunk(chunk);
                ix = 0;
            }
            chunk[ix++] = value;
        }
        String[] result = buffer.completeAndClearBuffer(chunk, ix, String.class);
        return result;
    } finally {
        ctxt.returnObjectBuffer(buffer);
    }
}