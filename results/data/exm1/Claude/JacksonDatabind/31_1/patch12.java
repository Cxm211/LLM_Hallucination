public void writeNumber(String encodedValue) throws IOException {
    _append(JsonToken.VALUE_NUMBER_FLOAT, encodedValue);
}