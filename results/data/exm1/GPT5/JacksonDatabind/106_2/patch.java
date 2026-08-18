public int getIntValue() throws IOException {
        final JsonNode node = currentNumericNode();
        if (!(node instanceof NumericNode)) {
            throw new JsonParseException(this, "Current token not numeric, cannot use getIntValue()");
        }
        return ((NumericNode) node).intValue();
    }