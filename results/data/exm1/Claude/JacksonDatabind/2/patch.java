public void writeObject(Object value) throws IOException
{
    if (_objectCodec == null) {
        throw new IllegalStateException("No ObjectCodec defined for the generator, can not serialize regular Java objects");
    }
    _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
}