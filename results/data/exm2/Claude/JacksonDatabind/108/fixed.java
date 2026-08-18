// ===== FIXED com.fasterxml.jackson.databind.ObjectReader :: readTree(JsonParser) [lines 1168-1170] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-108-fixed/src/main/java/com/fasterxml/jackson/databind/ObjectReader.java =====
    public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
        return (T) _bindAsTreeOrNull(p);
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