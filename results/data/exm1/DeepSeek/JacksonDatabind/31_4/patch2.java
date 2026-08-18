    public void writeRawValue(String text) throws IOException {
        if (text == null) {
            writeNull();
        } else {
            _append(JsonToken.VALUE_EMBEDDED_OBJECT, new RawValue(text));
        }
    }