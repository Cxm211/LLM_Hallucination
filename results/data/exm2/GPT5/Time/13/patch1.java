        public void printTo(StringBuffer buf, ReadablePeriod period, Locale locale) {
            long valueLong = getFieldValue(period);
            if (valueLong == Long.MAX_VALUE) {
                return;
            }
            int value = (int) valueLong;
            if (iFieldType >= SECONDS_MILLIS) {
                value = (int) (valueLong / DateTimeConstants.MILLIS_PER_SECOND);
            }

            if (iPrefix != null) {
                iPrefix.printTo(buf, value);
            }
            int minDigits = iMinPrintedDigits;
            boolean negativeZero = (iFieldType >= SECONDS_MILLIS) && (valueLong < 0) && (value == 0);
            if (negativeZero) {
                buf.append('-');
                if (minDigits <= 1) {
                    FormatUtils.appendUnpaddedInteger(buf, 0);
                } else {
                    FormatUtils.appendPaddedInteger(buf, 0, minDigits);
                }
            } else {
                if (minDigits <= 1) {
                    FormatUtils.appendUnpaddedInteger(buf, value);
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