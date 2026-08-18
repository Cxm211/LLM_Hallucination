    public void writeObject(Object value) throws IOException
    {
        if (value == null) {
            writeNull();
        } else if (_objectCodec != null) {
            _objectCodec.writeValue(this, value);
        } else {
            /* 28-May-2014, tatu: Tricky choice here; if no codec, should we
             *   err out, or just embed? For now, do latter.
             */
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
            // throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
        }
    }