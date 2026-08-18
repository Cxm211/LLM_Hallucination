    public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (hoursOffset < -23 || hoursOffset > 23) {
            throw new IllegalArgumentException("Hours out of range: " + hoursOffset);
        }
        if (minutesOffset < -59 || minutesOffset > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        if (hoursOffset > 0 && minutesOffset < 0) {
            throw new IllegalArgumentException("Minutes must be non-negative for positive hours");
        }
        int offset = 0;
        try {
            int totalMinutes;
            if (hoursOffset == 0) {
                totalMinutes = minutesOffset;
            } else if (hoursOffset > 0) {
                totalMinutes = hoursOffset * 60 + minutesOffset;
            } else {
                totalMinutes = hoursOffset * 60 - Math.abs(minutesOffset);
            }
            offset = FieldUtils.safeMultiply(totalMinutes, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }