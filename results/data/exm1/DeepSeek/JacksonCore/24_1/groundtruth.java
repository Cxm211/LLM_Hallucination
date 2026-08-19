// ===== FIXED com.fasterxml.jackson.core.base.ParserBase :: _reportTooLongIntegral(int, String) [lines 867-874] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-24-fixed/src/main/java/com/fasterxml/jackson/core/base/ParserBase.java =====
    protected void _reportTooLongIntegral(int expType, String rawNum) throws IOException
    {
        if (expType == NR_INT) {
            reportOverflowInt(rawNum);
        } else {
            reportOverflowLong(rawNum);
        }
    }

// ===== FIXED com.fasterxml.jackson.core.base.ParserBase :: convertNumberToInt() [lines 882-914] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-24-fixed/src/main/java/com/fasterxml/jackson/core/base/ParserBase.java =====
    protected void convertNumberToInt() throws IOException
    {
        // First, converting from long ought to be easy
        if ((_numTypesValid & NR_LONG) != 0) {
            // Let's verify it's lossless conversion by simple roundtrip
            int result = (int) _numberLong;
            if (((long) result) != _numberLong) {
                reportOverflowInt(getText(), currentToken());
            }
            _numberInt = result;
        } else if ((_numTypesValid & NR_BIGINT) != 0) {
            if (BI_MIN_INT.compareTo(_numberBigInt) > 0 
                    || BI_MAX_INT.compareTo(_numberBigInt) < 0) {
                reportOverflowInt();
            }
            _numberInt = _numberBigInt.intValue();
        } else if ((_numTypesValid & NR_DOUBLE) != 0) {
            // Need to check boundaries
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

// ===== FIXED com.fasterxml.jackson.core.base.ParserMinimalBase :: reportOverflowInt(String) [lines 564-566] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-24-fixed/src/main/java/com/fasterxml/jackson/core/base/ParserMinimalBase.java =====
    protected void reportOverflowInt(String numDesc) throws IOException {
        reportOverflowInt(numDesc, JsonToken.VALUE_NUMBER_INT);
    }

// ===== FIXED com.fasterxml.jackson.core.base.ParserMinimalBase :: reportOverflowLong(String) [lines 585-587] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-24-fixed/src/main/java/com/fasterxml/jackson/core/base/ParserMinimalBase.java =====
    protected void reportOverflowLong(String numDesc) throws IOException {
        reportOverflowLong(numDesc, JsonToken.VALUE_NUMBER_INT);
    }
