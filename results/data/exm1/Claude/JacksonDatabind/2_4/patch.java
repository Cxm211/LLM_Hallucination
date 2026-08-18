public void writeObject(Object value) throws IOException
{
    if (_objectCodec == null) {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    } else {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    }
}