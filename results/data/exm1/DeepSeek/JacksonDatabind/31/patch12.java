public void writeNumber(String encodedValue) throws IOException {
    if (encodedValue == null) {
        writeNull();
    } else {
        boolean isFloat = encodedValue.indexOf('.') >= 0
                || encodedValue.indexOf('e') >= 0
                || encodedValue.indexOf('E') >= 0;
        _append(isFloat ? JsonToken.VALUE_NUMBER_FLOAT : JsonToken.VALUE_NUMBER_INT, encodedValue);
    }
}