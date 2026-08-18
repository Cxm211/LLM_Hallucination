public void writeObject(Object value) throws IOException
{
    if (_objectCodec != null) {
        _objectCodec.writeValue(this, value);
    } else {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    }
}