    public void writeNumber(BigDecimal value) throws IOException
    {
        // Don't really know max length for big decimal, no point checking
        _verifyValueWrite(WRITE_NUMBER);
        if (value == null) {
            _writeNull();
        } else  if (_cfgNumbersAsStrings) {
            boolean plain = isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN);
            if (plain) {
                _verifyBigDecimalScale(value);
            }
            String raw = plain ? value.toPlainString() : value.toString();
            _writeQuotedRaw(raw);
        } else if (isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
            _verifyBigDecimalScale(value);
            writeRaw(value.toPlainString());
        } else {
            writeRaw(_asString(value));
        }
    }