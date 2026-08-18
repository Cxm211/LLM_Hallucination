protected String _asString(BigDecimal value) throws IOException {
    // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector
    int scale = value.scale();
    if (scale < -9999 || scale > 9999) {
        throw new JsonGenerationException(
            "Attempt to write plain `java.math.BigDecimal` (see JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN) with illegal scale (" + scale + "): minimum is -9999, maximum is 9999",
            this);
    }
    return value.toPlainString();
}