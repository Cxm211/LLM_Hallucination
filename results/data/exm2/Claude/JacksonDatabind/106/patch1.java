public long getLongValue() throws IOException {
    final NumericNode node = (NumericNode) currentNumericNode();
    if (!node.canConvertToLong()) {
        reportOverflowLong();
    }
    return node.longValue();
}

private void reportOverflowLong() throws InputCoercionException {
    throw new InputCoercionException(this, "Numeric value out of range of long", JsonToken.VALUE_NUMBER_LONG, Long.class);
}