    public <T extends JsonNode> T valueToTree(Object fromValue)
        throws IllegalArgumentException
    {
        if (fromValue == null) return null;
        TokenBuffer buf = new TokenBuffer(this, false);
        JsonNode result;
        try {
            SerializationConfig config = getSerializationConfig().without(SerializationFeature.WRAP_ROOT_VALUE);
            _serializerProvider(config).serializeValue(buf, fromValue);
            JsonParser jp = buf.asParser();
            result = readTree(jp);
            jp.close();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return (T) result;
    }