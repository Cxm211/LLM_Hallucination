// org/apache/commons/lang3/time/FastDateFormatTest.java
public void testLang538_AdditionalCase2() {
    final String dateTime = "2009-10-16T12:42:16.000Z";
    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT-4"));
    cal.clear();
    cal.set(2009, 9, 16, 8, 42, 16);
    FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
    assertEquals("dateTime", dateTime, format.format(cal));
}