// buggy function
    public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
        if (hoursOffset == 0 && minutesOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (minutesOffset < 0 || minutesOffset > 59) {
            throw new IllegalArgumentException("Minutes out of range: " + minutesOffset);
        }
        int offset = 0;
        try {
            int hoursInMinutes = FieldUtils.safeMultiply(hoursOffset, 60);
            if (hoursInMinutes < 0) {
                minutesOffset = FieldUtils.safeAdd(hoursInMinutes, -minutesOffset);
            } else {
                minutesOffset = FieldUtils.safeAdd(hoursInMinutes, minutesOffset);
            }
            offset = FieldUtils.safeMultiply(minutesOffset, DateTimeConstants.MILLIS_PER_MINUTE);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("Offset is too large");
        }
        return forOffsetMillis(offset);
    }

    public static DateTimeZone forOffsetMillis(int millisOffset) {
        String id = printOffset(millisOffset);
        return fixedOffsetZone(id, millisOffset);
    }

// trigger testcase
// org/joda/time/TestDateTimeZone.java::testForOffsetHoursMinutes_int_int
public void testForOffsetHoursMinutes_int_int() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
        assertEquals(DateTimeZone.forID("+23:59"), DateTimeZone.forOffsetHoursMinutes(23, 59));
        assertEquals(DateTimeZone.forID("+03:15"), DateTimeZone.forOffsetHoursMinutes(3, 15));
        assertEquals(DateTimeZone.forID("-02:00"), DateTimeZone.forOffsetHoursMinutes(-2, 0));
        assertEquals(DateTimeZone.forID("-02:30"), DateTimeZone.forOffsetHoursMinutes(-2, 30));
        assertEquals(DateTimeZone.forID("-23:59"), DateTimeZone.forOffsetHoursMinutes(-23, 59));
        try {
            DateTimeZone.forOffsetHoursMinutes(2, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-2, 60);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(2, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-2, -1);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(24, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            DateTimeZone.forOffsetHoursMinutes(-24, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
    }
