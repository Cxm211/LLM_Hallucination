// org/apache/commons/lang3/time/FastDateFormatTest.java
public void testLang645_AdditionalLocale() {
    Locale locale = new Locale("de", "DE");

    Calendar cal = Calendar.getInstance();
    cal.set(2010, 0, 4, 12, 0, 0);
    Date d = cal.getTime();

    FastDateFormat fdf = FastDateFormat.getInstance("EEEE', week 'ww", locale);

    assertEquals("Montag, week 01", fdf.format(d));
}