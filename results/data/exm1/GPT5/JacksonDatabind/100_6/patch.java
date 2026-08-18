// buggy code
    public byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException, JsonParseException
    {
        // Multiple possibilities...
        JsonNode n = currentNode();
        if (n != null) {
            // If textual, need to use provided Base64Variant for decoding
            if (n.isTextual()) {
                return ((TextNode) n).getBinaryValue(b64variant);
            }
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
        }
        // otherwise return null to mark we have no binary content
        return null;
    }