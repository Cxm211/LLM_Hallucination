public int getIntValue() throws IOException {
    final NumericNode node = (NumericNode) currentNumericNode();
    if (node == null) {
        return 0;
    }
    return node.intValue();
}