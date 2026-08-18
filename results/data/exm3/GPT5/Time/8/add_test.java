// org/joda/time/TestDateTimeZone.java::testForOffsetHoursMinutes_int_int_additional
public void testForOffsetHoursMinutes_int_int_additional() {
        assertEquals(DateTimeZone.forID("-23:59"), DateTimeZone.forOffsetHoursMinutes(-23, -59));
    }