public int getIntValue() throws IOException {
        final JsonNode node = currentNumericNode();
        return node.intValue();
    }

    public long getLongValue() throws IOException {
        final JsonNode node = currentNumericNode();
        return node.longValue();
    }