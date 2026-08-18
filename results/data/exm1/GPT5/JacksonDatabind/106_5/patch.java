public int getIntValue() throws IOException {
        final JsonNode node = currentNumericNode();
        if (node == null || !node.isNumber()) {
            throw new IOException("Current token not a numeric value");
        }
        return ((NumericNode) node).intValue();
    }