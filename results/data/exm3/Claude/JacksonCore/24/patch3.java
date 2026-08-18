protected void reportOverflowLong(String numDesc) throws IOException {
    _reportError(String.format("Numeric value (%s) out of range of long (%d - %d)",
            _longIntegerDesc(numDesc), Long.MIN_VALUE, Long.MAX_VALUE));
}