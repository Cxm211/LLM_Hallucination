public void writeTree(TreeNode node) throws IOException
{
    if (_objectCodec != null) {
        _objectCodec.writeTree(this, node);
    } else {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }
}