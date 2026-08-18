public <T extends JsonNode> T valueToTree(Object fromValue)
    throws IllegalArgumentException
{
    TokenBuffer buf = new TokenBuffer(this, false);
    JsonNode result;
    try {
        writeValue(buf, fromValue);
        JsonParser jp = buf.asParser();
        result = readTree(jp);
        jp.close();
    } catch (IOException e) { // should not occur, no real i/o...
        throw new IllegalArgumentException(e.getMessage(), e);
    }
    return (T) result;
}