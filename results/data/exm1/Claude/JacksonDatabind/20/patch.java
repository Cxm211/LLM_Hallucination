public JsonNode setAll(Map<String,? extends JsonNode> properties)
{
    if (properties == null) {
        return this;
    }
    for (Map.Entry<String,? extends JsonNode> en : properties.entrySet()) {
        String key = en.getKey();
        if (key != null) {
            JsonNode n = en.getValue();
            if (n == null) {
                n = nullNode();
            }
            _children.put(key, n);
        }
    }
    return this;
}