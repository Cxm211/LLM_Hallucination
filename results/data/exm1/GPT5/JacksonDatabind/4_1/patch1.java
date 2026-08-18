protected final String[] _deserializeCustom(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] chunk = buffer.resetAndStart();
        final JsonDeserializer<String> deser = _elementDeserializer;
        
        int ix = 0;
        JsonToken t;

        // Support both array and non-array input with custom element deserializer
        if (!jp.isExpectedStartArrayToken()) {
            String value;
            if (jp.hasToken(JsonToken.VALUE_NULL)) {
                value = deser.getNullValue();
            } else {
                value = deser.deserialize(jp, ctxt);
            }
            ctxt.returnObjectBuffer(buffer);
            return new String[] { value };
        }

        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            // Ok: no need to convert Strings, but must recognize nulls
            String value = (t == JsonToken.VALUE_NULL) ? deser.getNullValue() : deser.deserialize(jp, ctxt);
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