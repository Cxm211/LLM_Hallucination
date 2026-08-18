public void writeRawValue(String text) throws IOException {
        if (text == null) {
            writeNull();
            return;
        }
        _append(JsonToken.VALUE_EMBEDDED_OBJECT, new RawValue(text));
    }