    public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.isLong()) {
            throw new InputCoercionException(this,
                    "Numeric value (" + node.asText() + ") out of range of long",
                    currentToken(), Long.TYPE);
        }
        return node.longValue();
    }