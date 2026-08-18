    public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.canConvertToInt()) {
            throw new InputCoercionException(this,
                    "Numeric value ("+node.asText()+") out of range of int",
                    node.asToken(), Integer.TYPE);
        }
        return node.intValue();
    }