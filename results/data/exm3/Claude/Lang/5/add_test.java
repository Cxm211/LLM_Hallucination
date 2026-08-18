// org/apache/commons/lang3/LocaleUtilsTest.java
@Test
public void testLang865_AdditionalCase1() {
    assertValidToLocale("_US", "", "US", "");
    assertValidToLocale("_FR_V", "", "FR", "V");
    try {
        LocaleUtils.toLocale("_GB_");
        fail("Must be at least 5 chars if starts with underscore and has trailing underscore");
    } catch (final IllegalArgumentException iae) {
    }
    try {
        LocaleUtils.toLocale("_Ab");
        fail("Must be uppercase if starts with underscore");
    } catch (final IllegalArgumentException iae) {
    }
}