protected void convertNumberToInt() throws IOException
{
    if ((_numTypesValid & NR_LONG) != 0) {
        int result = (int) _numberLong;
        if (((long) result) != _numberLong) {
            _reportError("Numeric value ("+getText()+") out of range of int");
        }
        _numberInt = result;
    } else if ((_numTypesValid & NR_BIGINT) != 0) {
        if (BI_MIN_INT.compareTo(_numberBigInt) > 0 
                || BI_MAX_INT.compareTo(_numberBigInt) < 0) {
            reportOverflowInt();
        }
        _numberInt = _numberBigInt.intValue();
    } else if ((_numTypesValid & NR_DOUBLE) != 0) {
        if (_numberDouble < MIN_INT_D || _numberDouble > MAX_INT_D) {
            reportOverflowInt();
        }
        _numberInt = (int) _numberDouble;
    } else if ((_numTypesValid & NR_BIGDECIMAL) != 0) {
        if (BD_MIN_INT.compareTo(_numberBigDecimal) > 0 
            || BD_MAX_INT.compareTo(_numberBigDecimal) < 0) {
            reportOverflowInt();
        }
        _numberInt = _numberBigDecimal.intValue();
    } else {
        _throwInternal();
    }
    _numTypesValid |= NR_INT;
}