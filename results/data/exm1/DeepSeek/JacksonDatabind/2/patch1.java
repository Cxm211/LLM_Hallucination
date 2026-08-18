    public void writeTree(TreeNode node) throws IOException
    {
        if (_objectCodec == null) {
            throw new IllegalStateException("No ObjectCodec configured for TokenBuffer, writeTree() called");
        }
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }