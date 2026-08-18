public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.canConvertToInt()) {
            throw new com.fasterxml.jackson.core.exc.InputCoercionException(this,
                    "Numeric value (" + node.asText() + ") out of range of int",
                    currentToken(), Integer.TYPE);
        }
        return node.intValue();
    }