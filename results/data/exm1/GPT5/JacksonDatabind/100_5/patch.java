public byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException, JsonParseException
    {
        JsonNode n = currentNode();
        if (n != null) {
            // First handle textual nodes with specified Base64 variant
            if (n.isTextual()) {
                return ((TextNode) n).getBinaryValue(b64variant);
            }
            // For real binary nodes and embedded POJO nodes
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
        return null;
    }