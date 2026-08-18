public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
    long valueLong = getFieldValue(period);
    if (valueLong == Long.MAX_VALUE) {
        return 0;
    }
    int value;
    if (iFieldType >= SECONDS_MILLIS) {
        value = (int) (valueLong / DateTimeConstants.MILLIS_PER_SECOND);
    } else {
        value = (int) valueLong;
    }
    int sum = Math.max(FormatUtils.calculateDigitCount(value), iMinPrintedDigits);
    if (iFieldType >= SECONDS_MILLIS) {
        int dp = (int) (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND);
        if (iFieldType == SECONDS_OPTIONAL_MILLIS && dp == 0) {
            // no decimal part
        } else {
            sum += 4; // decimal point and three digits
        }
    }
    if (value < 0) {
        sum++;
    }
    if (iPrefix != null) {
        sum += iPrefix.calculatePrintedLength(value);
    }
    if (iSuffix != null) {
        sum += iSuffix.calculatePrintedLength(value);
    }
    return sum;
}