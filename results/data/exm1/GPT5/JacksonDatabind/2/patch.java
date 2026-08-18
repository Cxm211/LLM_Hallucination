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
            // If no codec, try JsonSerializable; otherwise embed as-is
            if (value instanceof com.fasterxml.jackson.core.JsonSerializable) {
                ((com.fasterxml.jackson.core.JsonSerializable) value).serialize(this, null);
                return;
            }
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    }