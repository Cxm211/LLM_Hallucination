public void writeTree(TreeNode node) throws IOException
{
    if (_objectCodec == null) {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    } else {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    }
}