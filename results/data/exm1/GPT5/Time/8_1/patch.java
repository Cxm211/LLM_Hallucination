public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (hoursOffset < -23 || hoursOffset > 23) {
            throw new IllegalArgumentException("Hours out of range: " + hoursOffset);
        }
        if (minutesOffset < 0 || minutesOffset > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        int totalMinutes;
        try {
            totalMinutes = Math.abs(hoursOffset) * 60 + minutesOffset;
            if (hoursOffset < 0) {
                totalMinutes = -totalMinutes;
            }
            int offset = FieldUtils.safeMultiply(totalMinutes, DateTimeConstants.MILLIS_PER_MINUTE);
            return forOffsetMillis(offset);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
    }