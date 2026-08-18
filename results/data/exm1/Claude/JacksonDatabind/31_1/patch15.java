public void writeObject(Object value) throws IOException
{
    if (value == null) {
        writeNull();
        return;
    }
    Class<?> raw = value.getClass();
    if (raw == byte[].class || (value instanceof RawValue)) {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
        return;
    }
    if (_objectCodec == null) {
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    } else {
        _objectCodec.writeValue(this, value);
    }
}