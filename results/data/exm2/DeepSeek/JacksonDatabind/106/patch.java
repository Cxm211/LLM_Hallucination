    public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (node.canConvertToInt()) {
            return node.intValue();
        } else {
            throw new InputCoercionException(this, "Numeric value (" + node.asText() + ") out of range of int", node.asText(), Integer.TYPE);
        }
    }