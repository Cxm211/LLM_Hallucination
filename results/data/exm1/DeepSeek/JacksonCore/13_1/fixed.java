// ===== FIXED com.fasterxml.jackson.core.json.JsonGeneratorImpl :: enable(Feature) [lines 119-125] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-13-fixed/src/main/java/com/fasterxml/jackson/core/json/JsonGeneratorImpl.java =====
    public JsonGenerator enable(Feature f) {
        super.enable(f);
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = false;
        }
        return this;
    }
