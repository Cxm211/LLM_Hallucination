// buggy function
    public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException
    {
            return new BigDecimal(numStr);
    }

    public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len)
            throws NumberFormatException
    {
            return new BigDecimal(buffer, offset, len);
    }

    public BigDecimal contentsAsDecimal()
        throws NumberFormatException
    {
        // Already got a pre-cut array?
        if (_resultArray != null) {
            return NumberInput.parseBigDecimal(_resultArray);
        }
        // Or a shared buffer?
        if (_inputStart >= 0) {
            return NumberInput.parseBigDecimal(_inputBuffer, _inputStart, _inputLen);
        }
        // Or if not, just a single buffer (the usual case)
        if (_segmentSize == 0) {
            return NumberInput.parseBigDecimal(_currentSegment, 0, _currentSize);
        }
        // If not, let's just get it aggregated...
        return NumberInput.parseBigDecimal(contentsAsArray());
    }

// trigger testcase
// com/fasterxml/jackson/core/json/TestParserNonStandard.java::testAllowNaN
public void testAllowNaN() throws Exception {
        _testAllowNaN(false);
        _testAllowNaN(true);
    }
