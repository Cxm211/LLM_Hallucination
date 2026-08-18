// org/apache/commons/lang3/time/FastDateFormatTest.java
public void testLang645_FrenchLocale() {
    Locale locale = Locale.FRENCH;

    Calendar cal = Calendar.getInstance();
    cal.set(2010, 0, 1, 12, 0, 0);
    Date d = cal.getTime();

    FastDateFormat fdf = FastDateFormat.getInstance("EEEE', week 'ww", locale);

    assertEquals("vendredi, week 53", fdf.format(d));
}