protected String _asString(BigDecimal value) throws IOException {
    // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector
    if (isEnabled(Feature.WRITE_BIGDECIMAL_AS_PLAIN)) {
        return value.toPlainString();
    }
    return value.toString();
}