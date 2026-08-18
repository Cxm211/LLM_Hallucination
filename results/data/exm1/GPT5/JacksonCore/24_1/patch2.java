protected void reportOverflowInt(String numDesc) throws IOException {
        _reportInputCoercion(String.format("Numeric value (%s) out of range of int (%d - %s)",
                _longIntegerDesc(numDesc), Integer.MIN_VALUE, Integer.MAX_VALUE),
                JsonToken.VALUE_NUMBER_INT, Integer.TYPE);
    }