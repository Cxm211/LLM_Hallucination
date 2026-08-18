public void writeTree(TreeNode node) throws IOException
    {

            // as with 'writeObject()', is codec optional?
            if (node == null) {
                writeNull();
                return;
            }
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }