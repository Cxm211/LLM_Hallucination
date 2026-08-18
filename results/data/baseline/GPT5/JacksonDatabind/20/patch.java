public JsonNode setAll(Map<String,? extends JsonNode> properties)
    {
        if (properties == null) {
            return this;
        }
        for (Map.Entry<String,? extends JsonNode> en : properties.entrySet()) {
            JsonNode n = en.getValue();
            if (n == null) {
                n = nullNode();
            }
            _children.put(en.getKey(), n);
        }
        return this;
    }