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
            // For TextNode, try to decode base64 using the variant
            if (n.isTextual()) {
                String text = n.textValue();
                if (text != null) {
                    try {
                        return b64variant.decode(text);
                    } catch (IllegalArgumentException e) {
                        // Not valid base64, fall through to return null
                    }
                }
            }
        }
        // otherwise return null to mark we have no binary content
        return null;
    }