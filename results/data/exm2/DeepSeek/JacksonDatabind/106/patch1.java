    public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (node.canConvertToLong()) {
            return node.longValue();
        } else {
            throw new InputCoercionException(this, "Numeric value (" + node.asText() + ") out of range of long", node.asText(), Long.TYPE);
        }
    }