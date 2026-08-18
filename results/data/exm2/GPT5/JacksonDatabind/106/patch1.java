public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.canConvertToLong()) {
            throw new com.fasterxml.jackson.core.exc.InputCoercionException(this,
                    "Numeric value (" + node.asText() + ") out of range of long",
                    currentToken(), Long.TYPE);
        }
        return node.longValue();
    }