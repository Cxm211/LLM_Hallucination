public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (node == null) {
            throw new InputCoercionException(this, "Current token not numeric", getCurrentToken(), Integer.TYPE);
        }
        if (!node.canConvertToInt()) {
            throw new InputCoercionException(this,
                    "Numeric value (" + node.asText() + ") out of range of int",
                    JsonToken.VALUE_NUMBER_INT, Integer.TYPE);
        }
        return node.intValue();
    }