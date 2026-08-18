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
            // [databind#2096]: handle TextNode explicitly
            if (n.isTextual()) {
                String text = n.textValue();
                try {
                    return b64variant.decode(text);
                } catch (IllegalArgumentException e) {
                    // rethrow as JsonParseException
                    throw new JsonParseException(null, "Failed to decode Base64 content", e);
                }
            }
        }
        // otherwise return null to mark we have no binary content
        return null;
    }