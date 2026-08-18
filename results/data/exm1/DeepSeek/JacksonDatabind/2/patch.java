    public void writeObject(Object value) throws IOException
    {
        if (_objectCodec == null) {
            throw new IllegalStateException("No ObjectCodec configured for TokenBuffer, writeObject() called");
        }
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    }