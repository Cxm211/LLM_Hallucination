public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.canConvertToInt()) {
            throw new com.fasterxml.jackson.core.exc.InputCoercionException(this,
                    "Numeric value (" + getText() + ") out of range of int",
                    getCurrentToken(), Integer.TYPE);
        }
        return node.intValue();
    }