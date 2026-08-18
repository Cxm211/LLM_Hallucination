// org/apache/commons/lang/time/FastDateFormatTest.java
public void test_changeDefault_Locale_DateTimeInstance_TimeZone() {
        Locale realDefaultLocale = Locale.getDefault();
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, timeZone, Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, timeZone, null);
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format3 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, timeZone, null);

            assertSame(Locale.GERMANY, format1.getLocale());
            assertSame(Locale.US, format2.getLocale());
            assertSame(Locale.GERMANY, format3.getLocale());
            assertTrue(format1 != format2);
            assertTrue(format2 != format3);
        } finally {
            Locale.setDefault(realDefaultLocale);
        }
    }
