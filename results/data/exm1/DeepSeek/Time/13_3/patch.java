public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
    long valueLong = getFieldValue(period);
    if (valueLong == Long.MAX_VALUE) {
        return 0;
    }

    int sum;
    if (iFieldType >= SECONDS_MILLIS) {
        int seconds = (int) (Math.abs(valueLong) / DateTimeConstants.MILLIS_PER_SECOND);
        int millis = (int) (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND);
        sum = Math.max(FormatUtils.calculateDigitCount(seconds), iMinPrintedDigits);
        if (iFieldType == SECONDS_OPTIONAL_MILLIS && millis == 0) {
            // no decimal part
        } else {
            sum = Math.max(sum, 4);
            sum++;  // for decimal point
        }
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