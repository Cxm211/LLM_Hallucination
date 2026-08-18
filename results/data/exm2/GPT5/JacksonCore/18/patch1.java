public void writeNumber(BigDecimal value) throws IOException
{
    // Don't really know max length for big decimal, no point checking
    _verifyValueWrite(WRITE_NUMBER);
    if (value == null) {
        _writeNull();
    } else if (_cfgNumbersAsStrings) {
        String raw;
        if (Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features)) {
            int scale = value.scale();
            if ((scale < -9999) || (scale > 9999)) {
                throw new JsonGenerationException("Attempt to write plain `java.math.BigDecimal` with illegal scale ("+scale+")");
            }
            raw = value.toPlainString();
        } else {
            raw = value.toString();
        }
        _writeQuotedRaw(raw);
    } else if (Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features)) {
        int scale = value.scale();
        if ((scale < -9999) || (scale > 9999)) {
            throw new JsonGenerationException("Attempt to write plain `java.math.BigDecimal` with illegal scale ("+scale+")");
        }
        writeRaw(value.toPlainString());
    } else {
        writeRaw(_asString(value));
    }
}