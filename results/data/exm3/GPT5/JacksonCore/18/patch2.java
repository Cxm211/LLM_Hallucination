public void writeNumber(BigDecimal value) throws IOException
    {
        // Don't really know max length for big decimal, no point checking
        _verifyValueWrite(WRITE_NUMBER);
        if (value == null) {
            _writeNull();
            return;
        }
        // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector when using plain notation
        if (isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
            int scale = value.scale();
            if (scale < -9999 || scale > 9999) {
                throw new JsonGenerationException("Attempt to write plain `java.math.BigDecimal` with illegal scale ("+scale+")", this);
            }
        }
        if (_cfgNumbersAsStrings) {
            String raw = isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN) ? value.toPlainString() : value.toString();
            _writeQuotedRaw(raw);
        } else if (isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
            writeRaw(value.toPlainString());
        } else {
            writeRaw(_asString(value));
        }
    }