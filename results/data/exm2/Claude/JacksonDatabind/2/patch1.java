public void writeTree(TreeNode node) throws IOException
{

        // as with 'writeObject()', is codec optional?
        if (_objectCodec == null) {
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
        } else {
            _objectCodec.writeTree(this, node);
        }
}