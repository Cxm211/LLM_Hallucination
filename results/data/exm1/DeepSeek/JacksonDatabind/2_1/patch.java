    public void writeObject(Object value) throws IOException
    {
        if (_codec == null) {
            throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
        }
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    }