    public byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException, JsonParseException
    {
        // Multiple possibilities...
        JsonNode n = currentNode();
        if (n != null) {
            // [databind#2096]: although `binaryValue()` works for real binary node
            // and embedded "POJO" node, coercion from TextNode may require variant, so:
            // First, handle direct binary and POJO cases
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
            // If textual, decode using provided Base64 variant
            if (n.isTextual()) {
                return ((com.fasterxml.jackson.databind.node.TextNode) n).getBinaryValue(b64variant);
            }
        }
        // otherwise return null to mark we have no binary content
        return null;
    }