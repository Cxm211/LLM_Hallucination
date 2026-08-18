public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (hoursOffset < -23 || hoursOffset > 23) {
            throw new IllegalArgumentException("Hours out of range: " + hoursOffset);
        }
        // Allow minutes in range -59..59 to support inputs like (0, -30) or (-1, -30).
        if (minutesOffset < -59 || minutesOffset > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        // Ensure minutes sign matches hours if hours is non-zero.
        if (hoursOffset > 0 && minutesOffset < 0) {
            minutesOffset = -minutesOffset;
        } else if (hoursOffset < 0 && minutesOffset > 0) {
            minutesOffset = -minutesOffset;
        }
        int offset;
        try {
            int totalMinutes = hoursOffset * 60 + minutesOffset;
            offset = FieldUtils.safeMultiply(totalMinutes, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }