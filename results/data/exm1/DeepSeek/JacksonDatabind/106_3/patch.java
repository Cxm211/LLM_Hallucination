public int getIntValue() throws IOException {
    final NumericNode node = (NumericNode) currentNumericNode();
    if (node == null) {
        throw new IOException("No numeric value to read");
    }
    return node.intValue();
}