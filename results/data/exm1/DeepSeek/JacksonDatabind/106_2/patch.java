    public int getIntValue() throws IOException {
        final JsonNode node = currentNumericNode();
        return node.asInt();
    }