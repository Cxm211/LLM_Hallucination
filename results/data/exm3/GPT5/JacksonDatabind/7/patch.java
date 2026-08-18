public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.FIELD_NAME) {
            // Missing START_OBJECT: synthesize object wrapper
            writeStartObject();
            do {
                copyCurrentStructure(jp);
                t = jp.nextToken();
            } while (t == JsonToken.FIELD_NAME);
            // Expecting END_OBJECT after last field
            writeEndObject();
        } else {
            copyCurrentStructure(jp);
        }
        return this;
    }