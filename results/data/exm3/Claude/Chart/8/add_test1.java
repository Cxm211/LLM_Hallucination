// org/jfree/data/time/junit/WeekTests.java
public void testConstructorWithJapaneseLocale() {
    Locale savedLocale = Locale.getDefault();
    TimeZone savedZone = TimeZone.getDefault();
    try {
        Locale.setDefault(Locale.JAPAN);
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
        
        GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance(
                TimeZone.getTimeZone("Asia/Tokyo"), Locale.JAPAN);
        // Japan's first day of week is Sunday
        assertEquals(Calendar.SUNDAY, cal.getFirstDayOfWeek());
        
        cal.set(2007, Calendar.AUGUST, 26, 1, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date t = cal.getTime();
        
        Week w = new Week(t, TimeZone.getTimeZone("Asia/Tokyo"));
        assertEquals(35, w.getWeek());
    } finally {
        Locale.setDefault(savedLocale);
        TimeZone.setDefault(savedZone);
    }
}