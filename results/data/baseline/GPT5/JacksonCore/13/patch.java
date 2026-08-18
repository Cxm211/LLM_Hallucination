public JsonGenerator enable(Feature f) {
        JsonGenerator gen = super.enable(f);
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = false;
        }
        return gen;
    }