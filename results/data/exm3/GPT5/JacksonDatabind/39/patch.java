public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        // If positioned on a field name, advance to its value first, then skip that value
        if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
            p.nextToken();
            p.skipChildren();
            return null;
        }
        // Otherwise just skip the current value/structure
        p.skipChildren();
        return null;
    }