protected String _asString(BigDecimal value) throws IOException {
    // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector
    if (isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
        int scale = value.scale();
        if (scale < -9999 || scale > 9999) {
            throw _constructError("Attempt to write plain `java.math.BigDecimal` ("+value.toString()+") with illegal scale ("+scale+"): suggested workaround is to use `JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN` to disable plain output");
        }
        return value.toPlainString();
    }
    return value.toString();
}