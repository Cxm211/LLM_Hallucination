    public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (node == null) {
            throw new IOException("Current token is not a numeric node");
        }
        return node.intValue();
    }