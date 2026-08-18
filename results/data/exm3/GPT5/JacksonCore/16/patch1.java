public JsonToken nextToken() throws IOException, JsonParseException
    {
        // If current delegate already has a current token (e.g., parser advanced before
        // being added to sequence), return it first without advancing.
        JsonToken t = delegate.currentToken();
        if (t != null) {
            delegate.clearCurrentToken();
            return t;
        }

        t = delegate.nextToken();
        if (t != null) {
            return t;
        }
        while (switchToNext()) {
            t = delegate.currentToken();
            if (t != null) {
                delegate.clearCurrentToken();
                return t;
            }
            t = delegate.nextToken();
            if (t != null) {
                return t;
            }
        }
        return null;
    }