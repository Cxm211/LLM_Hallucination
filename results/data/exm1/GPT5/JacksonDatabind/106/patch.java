public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (node == null) {
            throw new IOException("Current token is not a numeric value");
        }
        return node.intValue();
    }

    public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        if (node == null) {
            throw new IOException("Current token is not a numeric value");
        }
        return node.longValue();
    }