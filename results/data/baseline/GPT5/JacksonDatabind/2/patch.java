public void writeObject(Object value) throws IOException
    {
        if (value == null) {
            writeNull();
            return;
        }
        if (_objectCodec != null) {
            _objectCodec.writeValue(this, value);
            return;
        }
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    }