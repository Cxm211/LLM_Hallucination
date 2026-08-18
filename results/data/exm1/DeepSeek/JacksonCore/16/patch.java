    protected JsonParserSequence(JsonParser[] parsers)
    {
        super(parsers[0]);
        _parsers = parsers;
        _nextParser = 1;
        if (parsers[0].hasCurrentToken()) {
            _token = parsers[0].getCurrentToken();
            _hasToken = true;
        }
    }