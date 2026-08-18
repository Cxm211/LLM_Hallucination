public JsonNode setAll(Map<String,? extends JsonNode> properties)
    {
        for (Map.Entry<String,? extends JsonNode> en : properties.entrySet()) {
            JsonNode n = en.getValue();
            if (n == null) {
                throw new IllegalArgumentException("properties map contained null value for key '"+en.getKey()+"'");
            }
            _children.put(en.getKey(), n);
        }
        return this;
    }