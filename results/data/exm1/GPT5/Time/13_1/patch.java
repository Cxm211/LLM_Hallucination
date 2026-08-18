public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
            long valueLong = getFieldValue(period);
            if (valueLong == Long.MAX_VALUE) {
                return 0;
            }

            if (iFieldType >= SECONDS_MILLIS) {
                int seconds = (int) (valueLong / DateTimeConstants.MILLIS_PER_SECOND);
                int baseLen = Math.max(FormatUtils.calculateDigitCount(seconds), iMinPrintedDigits);
                // handle case where overall value is negative but seconds part is zero
                if (seconds == 0 && valueLong < 0) {
                    baseLen += 1; // for leading '-'
                }
                int dp = (int) (Math.abs(valueLong) % DateTimeConstants.MILLIS_PER_SECOND);
                if (iFieldType == SECONDS_MILLIS || dp > 0) {
                    baseLen += 4; // decimal point + 3 digits
                }
                if (iPrefix != null) {
                    baseLen += iPrefix.calculatePrintedLength(seconds);
                }
                if (iSuffix != null) {
                    baseLen += iSuffix.calculatePrintedLength(seconds);
                }
                return baseLen;
            }

            int value = (int) valueLong;
            int sum = Math.max(FormatUtils.calculateDigitCount(value), iMinPrintedDigits);
            if (iPrefix != null) {
                sum += iPrefix.calculatePrintedLength(value);
            }
            if (iSuffix != null) {
                sum += iSuffix.calculatePrintedLength(value);
            }

            return sum;
        }