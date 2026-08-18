    public String getValueAsString() throws IOException
    {
        if (_currToken == JsonToken.VALUE_STRING) {
            if (_tokenIncomplete) {
                String str = _finishAndReturnString();
                _tokenIncomplete = false;
                return str;
            }
            return _textBuffer.contentsAsString();
        }
        return super.getValueAsString(null);
    }