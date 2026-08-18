public void writeNumber(BigDecimal value) throws IOException
{
    // Don't really know max length for big decimal, no point checking
    _verifyValueWrite(WRITE_NUMBER);
    if (value == null) {
        _writeNull();
    } else  if (_cfgNumbersAsStrings) {
        if (Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features)) {
            int scale = value.scale();
            if (scale > 9999 || scale < -9999) {
                throw new JsonGenerationException("Illegal scale (" + scale + ") for BigDecimal when writing as plain string");
            }
        }
        String raw = Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features) ? value.toPlainString() : value.toString();
        _writeQuotedRaw(raw);
    } else if (Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features)) {
        int scale = value.scale();
        if (scale > 9999 || scale < -9999) {
            throw new JsonGenerationException("Illegal scale (" + scale + ") for BigDecimal when writing as plain string");
        }
        writeRaw(value.toPlainString());
    } else {
        writeRaw(_asString(value));
    }
}