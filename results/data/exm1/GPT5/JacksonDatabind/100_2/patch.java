public byte[] getBinaryValue(Base64Variant b64variant)
        throws IOException, JsonParseException
    {
        // Multiple possibilities...
        JsonNode n = currentNode();
        if (n != null) {
            // First try native binary value from node itself
            byte[] data = n.binaryValue();
            if (data != null) {
                return data;
            }
            // If textual, need to decode using provided Base64Variant
            if (n.isTextual()) {
                String txt = n.asText();
                if (txt != null) {
                    try {
                        return b64variant.decode(txt);
                    } catch (IllegalArgumentException e) {
                        throw new JsonParseException(null, e.getMessage(), e);
                    }
                }
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