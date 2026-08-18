// org/apache/commons/lang/time/FastDateFormatTest.java
public void test_changeDefault_Locale_DateInstance_withTimeZone() {
    Locale realDefaultLocale = Locale.getDefault();
    TimeZone realDefaultTimeZone = TimeZone.getDefault();
    try {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale.setDefault(Locale.US);
        FastDateFormat format1 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, Locale.GERMANY);
        FastDateFormat format2 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, null);
        Locale.setDefault(Locale.GERMANY);
        FastDateFormat format3 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, null);

        assertSame(Locale.GERMANY, format1.getLocale());
        assertSame(Locale.US, format2.getLocale());
        assertSame(Locale.GERMANY, format3.getLocale());
        assertTrue(format1 != format2);
        assertTrue(format2 != format3);

    } finally {
        Locale.setDefault(realDefaultLocale);
        TimeZone.setDefault(realDefaultTimeZone);
    }
}