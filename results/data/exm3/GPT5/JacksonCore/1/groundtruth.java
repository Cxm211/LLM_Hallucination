// ===== FIXED com.fasterxml.jackson.core.io.NumberInput :: parseBigDecimal(String) [lines 293-300] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-1-fixed/src/main/java/com/fasterxml/jackson/core/io/NumberInput.java =====
    public static BigDecimal parseBigDecimal(String numStr) throws NumberFormatException
    {
        try {
            return new BigDecimal(numStr);
        } catch (NumberFormatException e) {
            throw _badBigDecimal(numStr);
        }
    }

// ===== FIXED com.fasterxml.jackson.core.io.NumberInput :: parseBigDecimal(char[], int, int) [lines 306-314] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-1-fixed/src/main/java/com/fasterxml/jackson/core/io/NumberInput.java =====
    public static BigDecimal parseBigDecimal(char[] buffer, int offset, int len)
            throws NumberFormatException
    {
        try {
            return new BigDecimal(buffer, offset, len);
        } catch (NumberFormatException e) {
            throw _badBigDecimal(new String(buffer, offset, len));
        }
    }

// ===== FIXED com.fasterxml.jackson.core.util.TextBuffer :: contentsAsDecimal() [lines 382-399] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-1-fixed/src/main/java/com/fasterxml/jackson/core/util/TextBuffer.java =====
    public BigDecimal contentsAsDecimal()
        throws NumberFormatException
    {
        // Already got a pre-cut array?
        if (_resultArray != null) {
            return NumberInput.parseBigDecimal(_resultArray);
        }
        // Or a shared buffer?
        if ((_inputStart >= 0) && (_inputBuffer != null)) {
            return NumberInput.parseBigDecimal(_inputBuffer, _inputStart, _inputLen);
        }
        // Or if not, just a single buffer (the usual case)
        if ((_segmentSize == 0) && (_currentSegment != null)) {
            return NumberInput.parseBigDecimal(_currentSegment, 0, _currentSize);
        }
        // If not, let's just get it aggregated...
        return NumberInput.parseBigDecimal(contentsAsArray());
    }
