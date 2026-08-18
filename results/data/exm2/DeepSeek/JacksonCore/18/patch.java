protected String _asString(BigDecimal value) throws IOException {
    // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector
    int scale = value.scale();
    if (scale > 9999 || scale < -9999) {
        throw new JsonGenerationException("Illegal scale (" + scale + ") for BigDecimal");
    }
    return value.toString();
}