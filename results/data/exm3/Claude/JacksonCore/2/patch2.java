protected JsonToken _handleInvalidNumberStart(int ch, boolean negative) throws IOException
{
    if (ch == 'I') {
        if (_inputPtr >= _inputEnd) {
            if (!loadMore()) {
                _reportInvalidEOFInValue();
            }
        }
        ch = _inputBuffer[_inputPtr++];
        if (ch == 'N') {
            String match = negative ? "-INF" :"+INF";
            _matchToken(match, 3);
            if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                return resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
            _reportError("Non-standard token '"+match+"': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
        } else if (ch == 'n') {
            String match = negative ? "-Infinity" :"+Infinity";
            _matchToken(match, 3);
            if (isEnabled(Feature.ALLOW_NON_NUMERIC_NUMBERS)) {
                return resetAsNaN(match, negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
            }
            _reportError("Non-standard token '"+match+"': enable JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS to allow");
        }
    }
    reportUnexpectedNumberChar(ch, "expected digit (0-9) to follow minus sign, for valid numeric value");
    return null;
}