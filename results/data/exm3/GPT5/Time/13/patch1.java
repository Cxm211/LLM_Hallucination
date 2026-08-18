public void printTo(StringBuffer buf, ReadablePeriod period, Locale locale) {
            long valueLong = getFieldValue(period);
            if (valueLong == Long.MAX_VALUE) {
                return;
            }
            int value = (int) valueLong;
            boolean forceMinus = false;
            if (iFieldType >= SECONDS_MILLIS) {
                value = (int) (valueLong / DateTimeConstants.MILLIS_PER_SECOND);
                // If overall value is negative but the seconds part is zero, we must print a leading minus
                if (valueLong < 0 && value == 0) {
                    forceMinus = true;
                }
            }

            if (iPrefix != null) {
                iPrefix.printTo(buf, value);
            }
            int minDigits = iMinPrintedDigits;
            if (minDigits <= 1) {
                if (forceMinus) {
                    buf.append('-');
                    FormatUtils.appendUnpaddedInteger(buf, Math.abs(value));
                } else {
                    FormatUtils.appendUnpaddedInteger(buf, value);
                }
            } else {
                if (forceMinus) {
                    buf.append('-');
                    FormatUtils.appendPaddedInteger(buf, Math.abs(value), minDigits);
                } else {
                    FormatUtils.appendPaddedInteger(buf, value, minDigits);
                }
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