    public JsonGenerator enable(Feature f) {
        if (f == Feature.QUOTE_FIELD_NAMES) {
            _cfgUnqNames = false;
        }
        super.enable(f);
        return this;
    }