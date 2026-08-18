public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
    long valueLong = getFieldValue(period);
    if (valueLong == Long.MAX_VALUE) {
        return 0;
    }

    int sum;
    if (iFieldType >= SECONDS_MILLIS) {
        // Correctly compute seconds and millis for negative values
        long seconds;
        int millis;
        if (valueLong < 0) {
            seconds = (valueLong - (DateTimeConstants.MILLIS_PER_SECOND - 1)) / DateTimeConstants.MILLIS_PER_SECOND;
            millis = (int)(valueLong - (seconds * DateTimeConstants.MILLIS_PER_SECOND));
        } else {
            seconds = valueLong / DateTimeConstants.MILLIS_PER_SECOND;
            millis = (int)(valueLong % DateTimeConstants.MILLIS_PER_SECOND);
        }
        // Compute sum based on seconds part only, including minus sign if negative
        sum = Math.max(FormatUtils.calculateDigitCount(Math.abs(seconds)), iMinPrintedDigits);
        if (seconds < 0) {
            sum++; // for the minus sign
        }
        // Add length for decimal part if needed
        if (iFieldType == SECONDS_MILLIS || (iFieldType == SECONDS_OPTIONAL_MILLIS && millis != 0)) {
            sum += 4; // '.' and three digits
        }
        valueLong = seconds;
    } else {
        sum = Math.max(FormatUtils.calculateDigitCount(Math.abs(valueLong)), iMinPrintedDigits);
        if (valueLong < 0) {
            sum++; // for the minus sign
        }
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