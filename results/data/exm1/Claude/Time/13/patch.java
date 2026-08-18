public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
    long valueLong = getFieldValue(period);
    if (valueLong == Long.MAX_VALUE) {
        return 0;
    }

    int sum = Math.max(FormatUtils.calculateDigitCount(valueLong), iMinPrintedDigits);
    if (iFieldType >= SECONDS_MILLIS) {
        sum = Math.max(sum, 4);
        sum++;
        if (iFieldType == SECONDS_OPTIONAL_MILLIS &&
                (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND) == 0) {
            sum -= 4;
        }
        valueLong = valueLong / DateTimeConstants.MILLIS_PER_SECOND;
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