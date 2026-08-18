// org/jfree/data/time/junit/WeekTests.java
public void testConstructorWithDifferentTimeZones() {
    Locale savedLocale = Locale.getDefault();
    TimeZone savedZone = TimeZone.getDefault();
    try {
        Locale.setDefault(Locale.UK);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        
        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance(
                TimeZone.getTimeZone("GMT"), Locale.UK);
        cal.set(2007, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date t = cal.getTime();
        
        Week w1 = new Week(t, TimeZone.getTimeZone("GMT"));
        Week w2 = new Week(t, TimeZone.getTimeZone("America/New_York"));
        
        // The weeks should be different because the time zones are different
        // GMT: 2007-01-01 00:00:00 is week 1
        // America/New_York: 2007-01-01 00:00:00 GMT is 2006-12-31 19:00:00 EST (previous year)
        assertEquals(1, w1.getWeek());
        assertNotEquals(w1.getWeek(), w2.getWeek());
    } finally {
        Locale.setDefault(savedLocale);
        TimeZone.setDefault(savedZone);
    }
}