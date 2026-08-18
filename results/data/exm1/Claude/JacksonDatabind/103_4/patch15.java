public Object deserializeKey(String key, DeserializationContext ctxt)
        throws IOException
    {
        if (key == null) {
            return null;
        }
        try {
            Object result = _parse(key, ctxt);
            if (result != null) {
                return result;
            }
        } catch (Exception re) {
            String msg = re.getMessage();
            return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation, problem: (%s) %s",
                    re.getClass().getName(),
                    (msg == null) ? "N/A" : msg);
        }
        if (_keyClass.isEnum() && ctxt.getConfig().isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
            return null;
        }
        return ctxt.handleWeirdKey(_keyClass, key, "not a valid representation");
    }