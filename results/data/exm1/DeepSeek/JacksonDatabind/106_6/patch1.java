public long getLongValue() throws IOException {
    final NumericNode node = (NumericNode) currentNumericNode();
    if (node == null) {
        throw new JsonParseException(null, "No numeric value");
    }
    return node.longValue();
}