public long getLongValue() throws IOException {
    JsonNode node = currentNumericNode();
    if (node == null) {
        return 0L;
    }
    if (!(node instanceof NumericNode)) {
        return 0L;
    }
    return ((NumericNode) node).longValue();
}