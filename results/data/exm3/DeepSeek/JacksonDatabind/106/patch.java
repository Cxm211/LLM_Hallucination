    public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.isInt()) {
            throw new InputCoercionException(this,
                    "Numeric value (" + node.asText() + ") out of range of int",
                    currentToken(), Integer.TYPE);
        }
        return node.intValue();
    }