    public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (node == null) {
            throw _constructError("Current token not a numeric value");
        }
        return node.intValue();
    }