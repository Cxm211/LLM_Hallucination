    public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (!node.canConvertToLong()) {
            throw new InputCoercionException(this,
                    "Numeric value ("+node.asText()+") out of range of long",
                    node.asToken(), Long.TYPE);
        }
        return node.longValue();
    }