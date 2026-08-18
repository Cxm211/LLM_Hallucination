// org/apache/commons/lang/LocaleUtilsTest.java::testIsAvailableLocale
public void testIsAvailableLocale_NoInit() {
        Locale locale = Locale.FRANCE;
        boolean expected = Arrays.asList(Locale.getAvailableLocales()).contains(locale);
        assertEquals(expected, LocaleUtils.isAvailableLocale(locale));
        assertEquals(false, LocaleUtils.isAvailableLocale(null));
    }