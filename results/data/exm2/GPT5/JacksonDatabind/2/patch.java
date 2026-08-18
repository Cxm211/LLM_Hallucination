public void writeObject(Object value) throws IOException
{
    if (value == null) {
        _append(JsonToken.VALUE_NULL);
        return;
    }
    if (_objectCodec != null) {
        _objectCodec.writeValue(this, value);
        return;
    }
    if (value instanceof TreeNode) {
        writeTree((TreeNode) value);
        return;
    }
    _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
}