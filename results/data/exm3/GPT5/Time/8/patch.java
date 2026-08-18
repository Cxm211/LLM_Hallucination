public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (hoursOffset < -23 || hoursOffset > 23) {
            throw new IllegalArgumentException("Hours out of range: " + hoursOffset);
        }
        if (Math.abs(minutesOffset) > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        if (hoursOffset > 0 && minutesOffset < 0) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        int offset;
        try {
            int totalMinutes;
            if (hoursOffset < 0) {
                totalMinutes = hoursOffset * 60 - Math.abs(minutesOffset);
            } else {
                totalMinutes = hoursOffset * 60 + minutesOffset;
            }
            if (totalMinutes == 0) {
                return DateTimeZone.UTC;
            }
            offset = FieldUtils.safeMultiply(totalMinutes, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }