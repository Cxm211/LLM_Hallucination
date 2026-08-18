    public long getLongValue() throws IOException {
        JsonNode node = currentNumericNode();
        if (node instanceof NumericNode) {
            return ((NumericNode) node).longValue();
        }
        return 0L;
    }