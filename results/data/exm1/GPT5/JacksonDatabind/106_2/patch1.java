public long getLongValue() throws IOException {
        final JsonNode node = currentNumericNode();
        if (!(node instanceof NumericNode)) {
            throw new JsonParseException(this, "Current token not numeric, cannot use getLongValue()");
        }
        return ((NumericNode) node).longValue();
    }