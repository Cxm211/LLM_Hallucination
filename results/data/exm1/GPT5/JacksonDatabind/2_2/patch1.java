public void writeTree(TreeNode node) throws IOException
    {

            // as with 'writeObject()', is codec optional?
            if (node == null) {
                writeNull();
                return;
            }
            if (_objectCodec != null) {
                _objectCodec.writeTree(this, node);
            } else {
                _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
            }
    }