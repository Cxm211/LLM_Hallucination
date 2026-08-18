public int getIntValue() throws IOException {
        final JsonNode node = currentNumericNode();
        if (node == null) {
            throw new IOException("No current numeric value");
        }
        if (node.canConvertToInt()) {
            return node.intValue();
        }
        String txt = node.asText();
        int digits = (txt == null) ? 0 : txt.replaceFirst("^-", "").length();
        throw new com.fasterxml.jackson.databind.exc.InputCoercionException(
                null,
                "Numeric value (" + txt + ") out of range of int (Integer with " + digits + " digits)",
                JsonToken.VALUE_NUMBER_INT,
                Integer.class);
    }