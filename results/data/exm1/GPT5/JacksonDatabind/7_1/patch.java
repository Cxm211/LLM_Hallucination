public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == null) {
            t = jp.nextToken();
            if (t == null) {
                return this;
            }
        }
        if (t == JsonToken.FIELD_NAME) {
            writeStartObject();
            copyCurrentStructure(jp);
            writeEndObject();
        } else {
            copyCurrentStructure(jp);
        }
        /* 28-Oct-2014, tatu: As per #592, need to support a special case of starting from
         *    FIELD_NAME, which is taken to mean that we are missing START_OBJECT, but need
         *    to assume one did exist.
         */
        return this;
    }