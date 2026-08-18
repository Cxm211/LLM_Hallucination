// org/apache/commons/lang3/time/FastDateFormatTest.java::testLang645
public void testLang645_de() {
        Locale locale = Locale.GERMANY;

        Calendar cal = Calendar.getInstance();
        cal.set(2010, 0, 1, 12, 0, 0);
        Date d = cal.getTime();

        FastDateFormat fdf = FastDateFormat.getInstance("ww", locale);

        assertEquals("53", fdf.format(d));
    }