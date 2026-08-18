public String getValueAsString() throws IOException
{
    if (_currToken == JsonToken.VALUE_STRING) {
        if (_tokenIncomplete) {
            _tokenIncomplete = false;
            return _finishAndReturnString();
        }
        return _textBuffer.contentsAsString();
    }
    return super.getValueAsString(null);
}