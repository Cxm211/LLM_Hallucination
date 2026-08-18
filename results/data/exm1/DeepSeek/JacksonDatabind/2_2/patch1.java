public void writeTree(TreeNode node) throws IOException
{
    if (_codec != null) {
        _codec.writeTree(this, node);
        return;
    }
    _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
}