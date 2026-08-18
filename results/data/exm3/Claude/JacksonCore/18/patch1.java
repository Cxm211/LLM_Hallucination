public void writeNumber(BigDecimal value) throws IOException
{
    // Don't really know max length for big decimal, no point checking
    _verifyValueWrite(WRITE_NUMBER);
    if (value == null) {
        _writeNull();
    } else  if (_cfgNumbersAsStrings) {
        String raw = isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN) ? value.toPlainString() : value.toString();
        _writeQuotedRaw(raw);
    } else if (isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
        int scale = value.scale();
        if (scale < -9999 || scale > 9999) {
            throw _constructError("Attempt to write plain `java.math.BigDecimal` ("+value.toString()+") with illegal scale ("+scale+"): suggested workaround is to use `JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN` to disable plain output");
        }
        writeRaw(value.toPlainString());
    } else {
        writeRaw(_asString(value));
    }
}