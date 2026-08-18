// org/apache/commons/lang/time/FastDateFormatTest.java::test_changeDefault_Locale_DateTimeInstance
public void test_changeDefault_Locale_DateTimeInstance_withTimeZone() {
        Locale realDefaultLocale = Locale.getDefault();
        TimeZone realDefaultTz = TimeZone.getDefault();
        try {
            TimeZone tz = TimeZone.getTimeZone("GMT");
            Locale.setDefault(Locale.US);
            FastDateFormat f1 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, null);
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat f2 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, null);

            assertSame(Locale.US, f1.getLocale());
            assertSame(Locale.GERMANY, f2.getLocale());
            assertTrue(f1 != f2);
        } finally {
            Locale.setDefault(realDefaultLocale);
            TimeZone.setDefault(realDefaultTz);
        }
    }