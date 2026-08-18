    public void writeTree(TreeNode node) throws IOException
    {
        if (_codec == null) {
            throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeTree() called");
        }
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }