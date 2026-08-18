public String getValueAsString() throws IOException {
    if (_currToken == JsonToken.VALUE_STRING) {
        return getText();
    }
    if (_currToken == null || _currToken == JsonToken.VALUE_NULL || !_currToken.isScalarValue()) {
        return null;
    }
    return getText();
}