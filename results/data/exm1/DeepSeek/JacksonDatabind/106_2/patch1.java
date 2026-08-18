    public long getLongValue() throws IOException {
        final JsonNode node = currentNumericNode();
        return node.asLong();
    }