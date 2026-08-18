public String getValueAsString(String defaultValue) throws IOException {
    if (_currToken == JsonToken.VALUE_STRING) {
        return getText();
    }
    if (_currToken == null || _currToken == JsonToken.VALUE_NULL) {
        return defaultValue;
    }
    if (!_currToken.isScalarValue()) {
        return defaultValue;
    }
    return getText();
}