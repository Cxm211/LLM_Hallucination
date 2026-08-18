public JsonToken nextToken() throws IOException, JsonParseException
{
    if (_hasToken) {
        _hasToken = false;
        return delegate.currentToken();
    }
    JsonToken t = delegate.nextToken();
    if (t != null) return t;
    while (switchToNext()) {
        t = delegate.nextToken();
        if (t != null) return t;
    }
    return null;
}