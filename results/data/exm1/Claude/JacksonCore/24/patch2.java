protected void reportOverflowInt(String numDesc) throws IOException {
    _reportError(String.format("Numeric value (%s) out of range of int (%d - %d)",
            _longIntegerDesc(numDesc), Integer.MIN_VALUE, Integer.MAX_VALUE));
}