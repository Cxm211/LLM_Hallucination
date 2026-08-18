public String[] deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        // Handle special case: empty String as null, if enabled
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_STRING) {
            String str = jp.getText();
            if (str.length() == 0 && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                return null;
            }
        }
        // Ok: must point to START_ARRAY (or equivalent)
        if (!jp.isExpectedStartArrayToken()) {
            return handleNonArray(jp, ctxt);
        }
        if (_elementDeserializer != null) {
            return _deserializeCustom(jp, ctxt);
        }

        final ObjectBuffer buffer = ctxt.leaseObjectBuffer();
        Object[] chunk = buffer.resetAndStart();

        int ix = 0;

            while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
                // Ok: no need to convert Strings, but must recognize nulls
                String value;
                if (t == JsonToken.VALUE_STRING) {
                    value = jp.getText();
                } else if (t == JsonToken.VALUE_NULL) {
                    value = null; // since we have established that '_elementDeserializer == null' earlier
                } else {
                    value = _parseString(jp, ctxt);
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