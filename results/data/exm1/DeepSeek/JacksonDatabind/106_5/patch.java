    public int getIntValue() throws IOException {
        JsonNode node = currentNumericNode();
        if (node instanceof NumericNode) {
            return ((NumericNode) node).intValue();
        }
        return 0;
    }