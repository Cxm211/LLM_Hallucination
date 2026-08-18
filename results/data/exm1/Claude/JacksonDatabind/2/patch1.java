public void writeTree(TreeNode node) throws IOException
{
    if (_objectCodec == null) {
        throw new IllegalStateException("No ObjectCodec defined for the generator, can not serialize TreeNode objects");
    }
    _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
}