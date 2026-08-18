// org/apache/commons/lang3/time/FastDateFormatTest.java
public void testFormatWithDST() {
        final String dateTime = "2015-06-01T16:00:00.000Z";
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.clear();
        cal.set(2015, 5, 1, 12, 0, 0);
        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
        assertEquals(dateTime, format.format(cal));
    }
