// buggy function
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        return (T) _bindAsTree(p);
    }

    protected final JsonNode _bindAsTree(JsonParser p) throws IOException
    {
        // Need to inline `_initForReading()` due to tree reading handling end-of-input specially
        _config.initialize(p);
        if (_schema != null) {
            p.setSchema(_schema);
        }

        JsonToken t = p.getCurrentToken();
        if (t == null) {
            t = p.nextToken();
            if (t == null) {
                return _config.getNodeFactory().missingNode();
            }
        }
        final JsonNode resultNode;
        if (t == JsonToken.VALUE_NULL) {
            resultNode = _config.getNodeFactory().nullNode();
        } else {
            final DeserializationContext ctxt = createDeserializationContext(p);
            final JsonDeserializer<Object> deser = _findTreeDeserializer(ctxt);
            if (_unwrapRoot) {
                resultNode = (JsonNode) _unwrapAndDeserialize(p, ctxt, JSON_NODE_TYPE, deser);
            } else {
                resultNode = (JsonNode) deser.deserialize(p, ctxt);
                if (_config.isEnabled(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)) {
                    _verifyNoTrailingTokens(p, ctxt, JSON_NODE_TYPE);
                }
            }
        }
        return resultNode;
    }

// trigger testcase
// com/fasterxml/jackson/databind/node/EmptyContentAsTreeTest.java::testNullFromEOFWithParserAndReader
public void testNullFromEOFWithParserAndReader() throws Exception
    {
        try (JsonParser p = MAPPER.getFactory().createParser(EMPTY0)) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
        try (JsonParser p = MAPPER.getFactory().createParser(EMPTY1)) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
        try (JsonParser p = MAPPER.getFactory().createParser(new StringReader(EMPTY0))) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
        try (JsonParser p = MAPPER.getFactory().createParser(new StringReader(EMPTY1))) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }

        try (JsonParser p = MAPPER.getFactory().createParser(EMPTY0_BYTES)) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
        try (JsonParser p = MAPPER.getFactory().createParser(EMPTY1_BYTES)) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
        try (JsonParser p = MAPPER.getFactory().createParser(EMPTY1_BYTES, 0, EMPTY1_BYTES.length)) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }

        try (JsonParser p = MAPPER.getFactory().createParser(new ByteArrayInputStream(EMPTY0_BYTES))) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
        try (JsonParser p = MAPPER.getFactory().createParser(new ByteArrayInputStream(EMPTY1_BYTES))) {
            _assertNullTree(MAPPER.reader().readTree(p));
        }
    }
