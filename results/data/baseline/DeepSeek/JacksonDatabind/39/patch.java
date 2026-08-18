    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        if (p.currentToken() == JsonToken.FIELD_NAME) {
            p.nextToken();
            p.skipChildren();
        } else {
            p.skipChildren();
        }
        return null;
    }