protected java.util.Date _parseDate(String value, DeserializationContext ctxt)
        throws IOException
    {
        try {
            if (_isEmptyOrTextualNull(value)) {
                return (java.util.Date) getNullValue(ctxt);
            }
            return ctxt.parseDate(value);
        } catch (IllegalArgumentException iae) {
            String msg = iae.getMessage();
            return (java.util.Date) ctxt.handleWeirdStringValue(_valueClass, value,
                    "not a valid representation (error: %s)",
                    (msg == null) ? "N/A" : msg);
        }
    }