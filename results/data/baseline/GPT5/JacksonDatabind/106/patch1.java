public long getLongValue() throws IOException {
        Object nodeObj = currentNumericNode();
        if (!(nodeObj instanceof NumericNode)) {
            throw new IOException("Current node is not numeric");
        }
        final NumericNode node = (NumericNode) nodeObj;
        return node.longValue();
    }