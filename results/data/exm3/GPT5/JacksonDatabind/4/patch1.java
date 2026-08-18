protected final String[] _deserializeCustom(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] chunk = buffer.resetAndStart();
        final JsonDeserializer<String> deser = _elementDeserializer;
        
        int ix = 0;
        JsonToken t;

            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                // Ok: no need to convert Strings, but must recognize nulls
                String value;
                try {
                    value = (t == JsonToken.VALUE_NULL) ? deser.getNullValue() : deser.deserialize(jp, ctxt);
                } catch (Exception e) {
                    throw JsonMappingException.wrapWithPath(e, null, ix);
                }
                if (ix >= chunk.length) {
                    chunk = buffer.appendCompletedChunk(chunk);
                    ix = 0;
                }
                chunk[ix++] = value;
            }
            // note: pass String.class, not String[].class, as we need element type for error info
        String[] result = buffer.completeAndClearBuffer(chunk, ix, String.class);
        ctxt.returnObjectBuffer(buffer);
        return result;
    }