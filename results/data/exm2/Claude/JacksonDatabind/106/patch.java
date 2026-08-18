public int getIntValue() throws IOException {
    final NumericNode node = (NumericNode) currentNumericNode();
    if (!node.canConvertToInt()) {
        reportOverflowInt();
    }
    return node.intValue();
}

private void reportOverflowInt() throws InputCoercionException {
    throw new InputCoercionException(this, "Numeric value out of range of int", JsonToken.VALUE_NUMBER_INT, Integer.class);
}