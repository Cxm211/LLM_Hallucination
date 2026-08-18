public int getIntValue() throws IOException {
    JsonNode node = currentNumericNode();
    if (node == null) {
        return 0;
    }
    if (!(node instanceof NumericNode)) {
        return 0;
    }
    return ((NumericNode) node).intValue();
}