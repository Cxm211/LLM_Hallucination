public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.canConvertToLong()) {
            throw new com.fasterxml.jackson.core.exc.InputCoercionException(this,
                    "Numeric value (" + getText() + ") out of range of long",
                    getCurrentToken(), Long.TYPE);
        }
        return node.longValue();
    }