    public void writeRawValue(char[] text, int offset, int len) throws IOException {
        if (text == null) {
            writeNull();
            return;
        }
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, new RawValue(new String(text, offset, len)));
    }