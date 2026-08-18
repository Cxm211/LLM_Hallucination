public String getValueAsString() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING || _currToken == JsonToken.FIELD_NAME) {
            return getText();
        }
        return getValueAsString(null);
    }