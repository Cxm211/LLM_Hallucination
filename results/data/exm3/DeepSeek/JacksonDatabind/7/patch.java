    public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.FIELD_NAME) {
            // 28-Oct-2014, tatu: As per #592, need to support a special case of starting from
            //    FIELD_NAME, which is taken to mean that we are missing START_OBJECT, but need
            //    to assume one did exist.
            writeStartObject();
            do {
                copyCurrentStructure(jp);
                t = jp.getCurrentToken();
            } while (t == JsonToken.FIELD_NAME);
            // now t should be END_OBJECT, copy it as well
            copyCurrentStructure(jp);
        } else {
            copyCurrentStructure(jp);
        }
        return this;
    }