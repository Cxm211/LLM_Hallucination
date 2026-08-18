public long getLongValue() throws IOException {
    final NumericNode node = (NumericNode) currentNumericNode();
    if (node == null) {
        return 0L;
    }
    return node.longValue();
}