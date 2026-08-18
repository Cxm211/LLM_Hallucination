protected void reportOverflowLong(String numDesc) throws IOException {
        _reportInputCoercion(String.format("Numeric value (%s) out of range of long (%d - %s)",
                _longIntegerDesc(numDesc), Long.MIN_VALUE, Long.MAX_VALUE),
                JsonToken.VALUE_NUMBER_INT, Long.TYPE);
    }