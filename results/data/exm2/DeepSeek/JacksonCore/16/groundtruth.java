// ===== FIXED com.fasterxml.jackson.core.util.JsonParserSequence :: JsonParserSequence [lines 43-49] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-16-fixed/src/main/java/com/fasterxml/jackson/core/util/JsonParserSequence.java =====
    protected JsonParserSequence(JsonParser[] parsers)
    {
        super(parsers[0]);
        _suppressNextToken = delegate.hasCurrentToken();
        _parsers = parsers;
        _nextParser = 1;
    }

// ===== FIXED com.fasterxml.jackson.core.util.JsonParserSequence :: nextToken() [lines 106-121] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-16-fixed/src/main/java/com/fasterxml/jackson/core/util/JsonParserSequence.java =====
    public JsonToken nextToken() throws IOException
    {
        if (delegate == null) {
            return null;
        }
        if (_suppressNextToken) {
            _suppressNextToken = false;
            return delegate.currentToken();
        }
        JsonToken t = delegate.nextToken();
        while ((t == null) && switchToNext()) {
            t = delegate.hasCurrentToken()
                    ? delegate.currentToken() : delegate.nextToken();
        }
        return t;
    }
