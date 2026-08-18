public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
    long valueLong = getFieldValue(period);
    if (valueLong == Long.MAX_VALUE) {
        return 0;
    }

    int sum;
    if (iFieldType >= SECONDS_MILLIS) {
        // Handle seconds with optional millis
        boolean negative = valueLong < 0;
        long absValueLong = Math.abs(valueLong);
        int millisPart = (int) (absValueLong % DateTimeConstants.MILLIS_PER_SECOND);
        int printedInteger = (int) (valueLong / DateTimeConstants.MILLIS_PER_SECOND);
        boolean negativeZero = negative && printedInteger == 0;
        
        int integerLength;
        if (negativeZero) {
            integerLength = 1 + Math.max(1, iMinPrintedDigits);
        } else {
            int absPrintedInteger = Math.abs(printedInteger);
            int digitCount = FormatUtils.calculateDigitCount(absPrintedInteger);
            integerLength = (printedInteger < 0 ? 1 : 0) + Math.max(digitCount, iMinPrintedDigits);
        }
        
        sum = integerLength;
        if (iFieldType == SECONDS_MILLIS || millisPart > 0) {
            sum += 1 + 3; // decimal point and three digits
        }
        // reset valueLong for prefix/suffix calculation
        valueLong = valueLong / DateTimeConstants.MILLIS_PER_SECOND;
    } else {
        sum = Math.max(FormatUtils.calculateDigitCount(valueLong), iMinPrintedDigits);
    }
    
    int value = (int) valueLong;
    
    if (iPrefix != null) {
        sum += iPrefix.calculatePrintedLength(value);
    }
    if (iSuffix != null) {
        sum += iSuffix.calculatePrintedLength(value);
    }
    
    return sum;
}