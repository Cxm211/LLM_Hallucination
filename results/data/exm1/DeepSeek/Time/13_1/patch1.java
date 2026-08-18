public void printTo(StringBuffer buf, ReadablePeriod period, Locale locale) {
    long valueLong = getFieldValue(period);
    if (valueLong == Long.MAX_VALUE) {
        return;
    }
    int value;
    int millis = 0;
    if (iFieldType >= SECONDS_MILLIS) {
        // Correctly compute seconds and millis for negative values
        long seconds;
        if (valueLong < 0) {
            seconds = (valueLong - (DateTimeConstants.MILLIS_PER_SECOND - 1)) / DateTimeConstants.MILLIS_PER_SECOND;
            millis = (int)(valueLong - (seconds * DateTimeConstants.MILLIS_PER_SECOND));
        } else {
            seconds = valueLong / DateTimeConstants.MILLIS_PER_SECOND;
            millis = (int)(valueLong % DateTimeConstants.MILLIS_PER_SECOND);
        }
        value = (int) seconds;
    } else {
        value = (int) valueLong;
    }

    if (iPrefix != null) {
        iPrefix.printTo(buf, value);
    }
    int minDigits = iMinPrintedDigits;
    if (minDigits <= 1) {
        FormatUtils.appendUnpaddedInteger(buf, value);
    } else {
        FormatUtils.appendPaddedInteger(buf, value, minDigits);
    }
    if (iFieldType >= SECONDS_MILLIS) {
        if (iFieldType == SECONDS_MILLIS || millis > 0) {
            buf.append('.');
            FormatUtils.appendPaddedInteger(buf, millis, 3);
        }
    }
    if (iSuffix != null) {
        iSuffix.printTo(buf, value);
    }
}