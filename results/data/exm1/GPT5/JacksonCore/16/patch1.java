public JsonToken nextToken() throws IOException, JsonParseException
    {
        JsonToken t = delegate.getCurrentToken();
        if (t != null) {
            delegate.clearCurrentToken();
            return t;
        }
        t = delegate.nextToken();
        if (t != null) return t;
        while (switchToNext()) {
            t = delegate.getCurrentToken();
            if (t != null) {
                delegate.clearCurrentToken();
                return t;
            }
            t = delegate.nextToken();
            if (t != null) return t;
        }
        return null;
    }