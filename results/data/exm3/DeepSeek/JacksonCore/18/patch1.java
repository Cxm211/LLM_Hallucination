    public void writeNumber(BigDecimal value) throws IOException
    {
        // Don't really know max length for big decimal, no point checking
        _verifyValueWrite(WRITE_NUMBER);
        if (value == null) {
            _writeNull();
        } else  if (_cfgNumbersAsStrings) {
            boolean plain = Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features);
            if (plain) {
                _verifyBigDecimalScale(value);
            }
            String raw = plain ? value.toPlainString() : value.toString();
            _writeQuotedRaw(raw);
        } else if (Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features)) {
            _verifyBigDecimalScale(value);
            writeRaw(value.toPlainString());
        } else {
            writeRaw(_asString(value));
        }
    }