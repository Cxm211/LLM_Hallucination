public final JsonToken nextToken() throws IOException
{
    if (_currToken == JsonToken.FIELD_NAME) {
        return _nextAfterName();
    }
    _numTypesValid = NR_UNKNOWN;
    if (_tokenIncomplete) {
        _skipString();
    }
    int i = _skipWSOrEnd();
    if (i < 0) {
        close();
        return (_currToken = null);
    }
    _binaryValue = null;
    if (i == INT_RBRACKET) {
        _updateLocation();
        if (!_parsingContext.inArray()) {
            _reportMismatchedEndMarker(i, '}');
        }
        _parsingContext = _parsingContext.getParent();
        return (_currToken = JsonToken.END_ARRAY);
    }
    if (i == INT_RCURLY) {
        _updateLocation();
        if (!_parsingContext.inObject()) {
            _reportMismatchedEndMarker(i, ']');
        }
        _parsingContext = _parsingContext.getParent();
        return (_currToken = JsonToken.END_OBJECT);
    }
    if (_parsingContext.expectComma()) {
        i = _skipComma(i);
    }
    _updateLocation();
    boolean inObject = _parsingContext.inObject();
    if (inObject) {
        String name = (i == INT_QUOTE) ? _parseName() : _handleOddName(i);
        _parsingContext.setCurrentName(name);
        _currToken = JsonToken.FIELD_NAME;
        i = _skipColon();
    }
    JsonToken t;
    switch (i) {
    case '"':
        _tokenIncomplete = true;
        t = JsonToken.VALUE_STRING;
        break;
    case '[':
        if (!inObject) {
            _parsingContext = _parsingContext.createChildArrayContext(_tokenInputRow, _tokenInputCol);
        }
        t = JsonToken.START_ARRAY;
        break;
    case '{':
        if (!inObject) {
            _parsingContext = _parsingContext.createChildObjectContext(_tokenInputRow, _tokenInputCol);
        }
        t = JsonToken.START_OBJECT;
        break;
    case ']':
    case '}':
        _reportUnexpectedChar(i, "expected a value");
    case 't':
        _matchTrue();
        t = JsonToken.VALUE_TRUE;
        break;
    case 'f':
        _matchFalse();
        t = JsonToken.VALUE_FALSE;
        break;
    case 'n':
        _matchNull();
        t = JsonToken.VALUE_NULL;
        break;
    case '-':
        t = _parseNegNumber();
        break;
    case '0':
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    case '7':
    case '8':
    case '9':
        t = _parsePosNumber(i);
        break;
    default:
        t = _handleOddValue(i);
        break;
    }
    if (inObject) {
        _nextToken = t;
        return _currToken;
    }
    _currToken = t;
    return t;
}