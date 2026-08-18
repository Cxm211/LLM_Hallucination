public void writeObject(Object value) throws IOException
{
    if (_hasNativeObjectIds) {
        _checkNativeIds();
    }
    if (_objectCodec == null) {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    } else {
        _objectCodec.writeValue(this, value);
    }
}