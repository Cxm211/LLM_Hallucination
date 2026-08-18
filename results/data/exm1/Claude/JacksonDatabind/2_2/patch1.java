public void writeTree(TreeNode node) throws IOException
{
    if (_hasNativeObjectIds) {
        _checkNativeIds();
    }
    if (_objectCodec == null) {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, node);
    } else {
        _objectCodec.writeTree(this, node);
    }
}