public byte[] getBinaryValue(Base64Variant b64variant)
    throws IOException, JsonParseException
{
    // Multiple possibilities...
    JsonNode n = currentNode();
    if (n != null) {
        // [databind#2096]: although `binaryValue()` works for real binary node
        // and embedded "POJO" node, coercion from TextNode may require variant, so:
        byte[] data = n.binaryValue();
        if (data != null) {
            return data;
        }
        if (n.isPojo()) {
            Object ob = ((POJONode) n).getPojo();
            if (ob instanceof byte[]) {
                return (byte[]) ob;
            }
        }
        if (n.isTextual()) {
            String text = n.textValue();
            try {
                return b64variant.decode(text);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException(this,
                        "Cannot base64 decode the content: " + e.getMessage(),
                        e);
            }
        }
    }
    // otherwise return null to mark we have no binary content
    return null;
}