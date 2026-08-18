public JsonGenerator enable(Feature f) {
        super.enable(f);
        // Recompute unquoted-names flag based on current state to ensure consistency
        _cfgUnqNames = !isEnabled(Feature.QUOTE_FIELD_NAMES);
        return this;
    }