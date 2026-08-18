public JsonGenerator enable(Feature f) {
        super.enable(f);
        // Keep internal flag in sync with current QUOTE_FIELD_NAMES setting
        _cfgUnqNames = !isEnabled(Feature.QUOTE_FIELD_NAMES);
        return this;
    }