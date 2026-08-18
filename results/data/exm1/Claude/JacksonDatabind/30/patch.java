public <T extends JsonNode> T valueToTree(Object fromValue)
        throws IllegalArgumentException
    {
        if (fromValue == null) return null;
        TokenBuffer buf = new TokenBuffer(this, false);
        JsonNode result;
        try {
            writeValue(buf, fromValue);
            JsonParser jp = buf.asParser();
            result = readTree(jp);
            jp.close();
            buf.close();
        } catch (IOException e) { 
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return (T) result;
    }