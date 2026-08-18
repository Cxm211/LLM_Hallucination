public void writeObject(Object value) throws IOException
{
    if (_codec != null) {
        _codec.writeValue(this, value);
        return;
    }
    _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
}