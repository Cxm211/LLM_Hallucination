public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        // Validate ranges similar to Joda-Time behavior
        if (hoursOffset < -23 || hoursOffset > 23) {
            throw new IllegalArgumentException("Hours out of range: " + hoursOffset);
        }
        if (minutesOffset < -59 || minutesOffset > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        // Disallow mixed signs (e.g., -1 hour and +30 minutes)
        if ((hoursOffset < 0 && minutesOffset > 0) || (hoursOffset > 0 && minutesOffset < 0)) {
            throw new IllegalArgumentException("Positive and negative offsets must be in the same direction");
        }
        int totalMinutes;
        try {
            int hoursInMinutes = FieldUtils.safeMultiply(Math.abs(hoursOffset), 60);
            totalMinutes = FieldUtils.safeAdd(hoursInMinutes, Math.abs(minutesOffset));
            if (hoursOffset < 0 || minutesOffset < 0) {
                totalMinutes = -totalMinutes;
            }
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        int offset;
        try {
            offset = FieldUtils.safeMultiply(totalMinutes, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }