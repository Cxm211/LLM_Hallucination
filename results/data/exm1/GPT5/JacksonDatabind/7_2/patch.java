public TokenBuffer deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
            // Handle special case where parsing starts at FIELD_NAME: synthesize object wrapper
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.FIELD_NAME) {
                writeStartObject();
                do {
                    writeFieldName(jp.getCurrentName());
                    jp.nextToken();
                    copyCurrentStructure(jp);
                    t = jp.nextToken();
                } while (t == JsonToken.FIELD_NAME);
                writeEndObject();
                return this;
            }
            copyCurrentStructure(jp);
        /* 28-Oct-2014, tatu: As per #592, need to support a special case of starting from
         *    FIELD_NAME, which is taken to mean that we are missing START_OBJECT, but need
         *    to assume one did exist.
         */
        return this;
    }