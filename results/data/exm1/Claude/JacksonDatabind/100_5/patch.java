public byte[] getBinaryValue(Base64Variant b64variant)
    throws IOException, JsonParseException
{
    JsonNode n = currentNode();
    if (n != null) {
        byte[] data = n.binaryValue();
        if (data != null) {
            return data;
        }
        if (n.isTextual()) {
            String text = n.asText();
            if (text != null) {
                return b64variant.decode(text);
            }
        }
        if (n.isPojo()) {
            Object ob = ((POJONode) n).getPojo();
            if (ob instanceof byte[]) {
                return (byte[]) ob;
            }
        }
    }
    return null;
}