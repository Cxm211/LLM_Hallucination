public String getValueAsString_Basic() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            return getText();
        }
        return getValueAsString(null);
    }