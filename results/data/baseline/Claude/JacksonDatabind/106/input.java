// buggy code
    public int getIntValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        return node.intValue();
    }

    public long getLongValue() throws IOException {
        final NumericNode node = (NumericNode) currentNumericNode();
        return node.longValue();
    }

