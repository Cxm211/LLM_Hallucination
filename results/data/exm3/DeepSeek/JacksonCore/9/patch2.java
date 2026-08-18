    public final String getValueAsString() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                _finishString();
                _tokenIncomplete = false;
            }
            return _textBuffer.contentsAsString();
        }
        return super.getValueAsString(null);
    }