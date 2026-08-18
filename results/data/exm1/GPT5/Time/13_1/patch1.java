public void printTo(StringBuffer buf, ReadablePeriod period, Locale locale) {
            long valueLong = getFieldValue(period);
            if (valueLong == Long.MAX_VALUE) {
                return;
            }
            if (iFieldType >= SECONDS_MILLIS) {
                int seconds = (int) (valueLong / DateTimeConstants.MILLIS_PER_SECOND);

                if (iPrefix != null) {
                    iPrefix.printTo(buf, seconds);
                }
                int minDigits = iMinPrintedDigits;
                // ensure negative sign is printed when seconds part is zero but overall is negative
                if (valueLong < 0 && seconds == 0) {
                    buf.append('-');
                }
                if (minDigits <= 1) {
                    FormatUtils.appendUnpaddedInteger(buf, seconds);
                } else {
                    FormatUtils.appendPaddedInteger(buf, seconds, minDigits);
                }
                int dp = (int) (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND);
                if (iFieldType == SECONDS_MILLIS || dp > 0) {
                    buf.append('.');
                    FormatUtils.appendPaddedInteger(buf, dp, 3);
                }
                if (iSuffix != null) {
                    iSuffix.printTo(buf, seconds);
                }
                return;
            }

            int value = (int) valueLong;

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
                int dp = (int) (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND);
                if (iFieldType == SECONDS_MILLIS || dp > 0) {
                    buf.append('.');
                    FormatUtils.appendPaddedInteger(buf, dp, 3);
                }
            }
            if (iSuffix != null) {
                iSuffix.printTo(buf, value);
            }
        }