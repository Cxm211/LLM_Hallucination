public void writeObject(Object value) throws IOException
    {
            if (value == null) {
                writeNull();
                return;
            }
            if (_objectCodec != null) {
                if (value instanceof TreeNode) {
                    writeTree((TreeNode) value);
                } else {
                    _objectCodec.writeValue(this, value);
                }
                return;
            }
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, value);
    }