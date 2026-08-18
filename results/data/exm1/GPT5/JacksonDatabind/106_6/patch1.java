public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (node == null) {
            throw new InputCoercionException(this, "Current token not numeric", getCurrentToken(), Long.TYPE);
        }
        if (!node.canConvertToLong()) {
            throw new InputCoercionException(this,
                    "Numeric value (" + node.asText() + ") out of range of long",
                    JsonToken.VALUE_NUMBER_INT, Long.TYPE);
        }
        return node.longValue();
    }