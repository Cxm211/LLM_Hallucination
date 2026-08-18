public void writeTree(TreeNode node) throws IOException
{
    if (node == null) {
        writeNull();
        return;
    }
    if (_objectCodec == null) {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    } else {
        _objectCodec.writeTree(this, node);
    }
}