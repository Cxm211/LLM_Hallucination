public void writeTree(TreeNode node) throws IOException
    {
            if (node == null) {
                writeNull();
                return;
            }
            if (_objectCodec != null) {
                _objectCodec.writeTree(this, node);
                return;
            }
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }