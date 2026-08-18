public BigDecimal contentsAsDecimal()
    throws NumberFormatException
{
    if (_resultArray != null) {
        return NumberInput.parseBigDecimal(_resultArray);
    }
    if (_inputStart >= 0) {
        return NumberInput.parseBigDecimal(_inputBuffer, _inputStart, _inputLen);
    }
    if (_segmentSize == 0) {
        return NumberInput.parseBigDecimal(_currentSegment, 0, _currentSize);
    }
    return NumberInput.parseBigDecimal(contentsAsArray());
}