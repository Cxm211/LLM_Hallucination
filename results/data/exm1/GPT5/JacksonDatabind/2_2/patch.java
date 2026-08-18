public void writeObject(Object value) throws IOException
    {
            if (value == null) {
                writeNull();
                return;
            }
            // If it's a TreeNode, delegate to writeTree so that structure is properly written
            if (value instanceof com.fasterxml.jackson.databind.JsonNode || value instanceof com.fasterxml.jackson.core.TreeNode) {
                writeTree((TreeNode) value);
                return;
            }
            // Otherwise, embed as-is (important for cases like byte[])
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
            /* 28-May-2014, tatu: Tricky choice here; if no codec, should we
             *   err out, or just embed? For now, do latter.
             */
//          throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
    }