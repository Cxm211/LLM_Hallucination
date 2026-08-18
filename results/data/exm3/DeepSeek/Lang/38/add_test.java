// org/apache/commons/lang3/time/FastDateFormatTest.java
public void testFormatWithTimeZoneOffset() {
        final String dateTime = "2010-01-01T05:00:00.000+00:00";
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+5:30"));
        cal.clear();
        cal.set(2010, 0, 1, 10, 30, 0);
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS XXX", TimeZone.getTimeZone("GMT"));
        assertEquals(dateTime, format.format(cal));
    }
