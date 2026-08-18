    public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        /* 28-Oct-2014, tatu: As per #592, need to support a special case of starting from
         *    FIELD_NAME, which is taken to mean that we are missing START_OBJECT, but need
         *    to assume one did exist.
         */
        if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
            writeStartObject();
        }
        copyCurrentStructure(jp);
        return this;
    }