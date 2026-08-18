        public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
            long valueLong = getFieldValue(period);
            if (valueLong == Long.MAX_VALUE) {
                return 0;
            }

            int sum = Math.max(FormatUtils.calculateDigitCount(valueLong), iMinPrintedDigits);
            if (iFieldType >= SECONDS_MILLIS) {
                // valueLong contains the seconds and millis fields
                // the minimum output is 0.000, which is 4 or 5 digits with a negative
                sum = Math.max(sum, 4);
                // plus one for the decimal point
                sum++;
                int dpAbs = (int) (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND);
                if (iFieldType == SECONDS_OPTIONAL_MILLIS && dpAbs == 0) {
                    sum -= 4; // remove three digits and decimal point
                }
                // account for negative zero seconds where only millis are negative (e.g. -0.012)
                long secondsPart = valueLong / DateTimeConstants.MILLIS_PER_SECOND;
                if (valueLong < 0 && secondsPart == 0 && (iFieldType == SECONDS_MILLIS || dpAbs > 0)) {
                    sum += 1; // for the '-'
                }
                // reset valueLong to refer to the seconds part for the prefix/suffix calculation
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

            // Handle the special case of negative zero seconds with fractional millis: print leading '-'
            if (iFieldType >= SECONDS_MILLIS) {
                int dp = (int) (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND);
                if (valueLong < 0 && value == 0 && (iFieldType == SECONDS_MILLIS || dp > 0)) {
                    buf.append('-');
                }
            }

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