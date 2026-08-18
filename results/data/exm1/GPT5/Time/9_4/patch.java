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
        // Enforce that hours and minutes have the same sign (or either is zero)
        if ((hoursOffset > 0 && minutesOffset < 0) || (hoursOffset < 0 && minutesOffset > 0)) {
            throw new IllegalArgumentException("Hours and minutes must have the same sign");
        }
        int offset;
        try {
            int hoursInMinutes = FieldUtils.safeMultiply(hoursOffset, 60);
            int totalMinutes = FieldUtils.safeAdd(hoursInMinutes, minutesOffset);
            offset = FieldUtils.safeMultiply(totalMinutes, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }

    public static DateTimeZone forOffsetMillis(int millisOffset) {
        if (millisOffset == 0) {
            return DateTimeZone.UTC;
        }
        String id = printOffset(millisOffset);
        return fixedOffsetZone(id, millisOffset);
    }