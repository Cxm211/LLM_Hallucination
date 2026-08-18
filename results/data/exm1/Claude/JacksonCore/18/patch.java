protected String _asString(BigDecimal value) throws IOException {
    if (Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(_features)) {
        return value.toPlainString();
    }
    return value.toString();
}