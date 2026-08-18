public int getIntValue() throws IOException {
    final JsonNode node = currentNumericNode();
    if (node == null || !node.isNumber()) {
        throw new IOException("Current token not a numeric value");
    }
    if (node instanceof NumericNode) {
        return ((NumericNode) node).intValue();
    }
    return node.asInt();
}