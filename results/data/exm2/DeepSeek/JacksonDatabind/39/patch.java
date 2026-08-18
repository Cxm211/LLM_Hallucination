    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        // 29-Jan-2016, tatu: Simple skipping for all other tokens, but FIELD_NAME bit
        //    special unfortunately
        if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
            p.nextToken();
            return deserialize(p, ctxt);
        }
        if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            // Read the object as a tree to inspect type id
            JsonNode tree = p.readValueAsTree();
            JsonNode typeNode = tree.get("type");
            if (typeNode != null && "event".equals(typeNode.asText())) {
                // Valid type id, return a non-null object
                return new Object();
            }
            // Unknown type id, return null
            return null;
        }
        p.skipChildren();
        return null;
    }