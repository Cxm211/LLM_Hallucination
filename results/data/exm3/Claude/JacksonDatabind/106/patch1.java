public long getLongValue() throws IOException {
    final NumericNode node = (NumericNode) currentNumericNode();
    if (!node.canConvertToLong()) {
        reportOverflowLong();
    }
    return node.longValue();
}