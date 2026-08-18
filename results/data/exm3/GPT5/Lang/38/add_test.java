// org/apache/commons/lang3/time/FastDateFormatTest.java::testLang538
public void testLang538_extremeTimezone() {
        final String dateTime = "2009-10-15T10:30:00.000Z";

        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+14"));
        cal.clear();
        cal.set(2009, 9, 16, 0, 30, 0);

        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
        assertEquals("dateTime", dateTime, format.format(cal));
    }