public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        // 29-Jan-2016, tatu: Simple skipping for all other tokens, but FIELD_NAME bit
        // special unfortunately: need to advance to value to properly skip container values
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.FIELD_NAME) {
            t = p.nextToken();
            if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                p.skipChildren();
            }
        } else if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
            p.skipChildren();
        }
        return null;
    }