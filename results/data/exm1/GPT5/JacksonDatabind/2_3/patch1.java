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
            // as with 'writeObject()', is codec optional?
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }