public String getValueAsString() throws IOException {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _tokenIncomplete = false;
                _finishString();
            }
            return _textBuffer.contentsAsString();
        }
        if (_currToken == null || _currToken == JsonToken.VALUE_NULL || !_currToken.isScalarValue()) {
            return null;
        }
        return getText();
    }