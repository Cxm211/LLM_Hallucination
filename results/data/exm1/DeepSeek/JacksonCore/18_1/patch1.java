public void writeNumber(BigDecimal value) throws IOException
{
        _verifyValueWrite(WRITE_NUMBER);
        if (value == null) {
            _writeNull();
        } else  if (_cfgNumbersAsStrings) {
            String raw = isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN) ? value.toPlainString() : value.toString();
            _writeQuotedRaw(raw);
        } else if (isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
            writeRaw(value.toPlainString());
        } else {
            writeRaw(_asString(value));
        }
    }