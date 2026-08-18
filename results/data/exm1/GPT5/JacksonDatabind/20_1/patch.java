public ObjectNode setAll(Map<String,? extends JsonNode> properties)
    {
        for (Map.Entry<String,? extends JsonNode> en : properties.entrySet()) {
            String key = en.getKey();
            if (key == null) {
                throw new IllegalArgumentException("Null key for ObjectNode.setAll()");
            }
            JsonNode n = en.getValue();
            if (n == null) {
                n = nullNode();
            }
            _children.put(key, n);
        }
        return this;
    }