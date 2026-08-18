protected JsonParserSequence(JsonParser[] parsers)
    {
        if (parsers == null || parsers.length == 0) {
            throw new IllegalArgumentException("Cannot create JsonParserSequence with no parsers");
        }
        for (JsonParser p : parsers) {
            if (p == null) {
                throw new IllegalArgumentException("Parsers array contains null");
            }
        }
        super(parsers[0]);
        _parsers = parsers;
        _nextParser = 1;
    }