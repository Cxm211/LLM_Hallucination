// org/apache/commons/lang3/LocaleUtilsTest.java
@Test
public void testLang865_AdditionalCase2() {
    assertValidToLocale("_CA_WIN", "", "CA", "WIN");
    try {
        LocaleUtils.toLocale("_A1");
        fail("Second char must be uppercase letter if starts with underscore");
    } catch (final IllegalArgumentException iae) {
    }
    try {
        LocaleUtils.toLocale("_1A");
        fail("First char after underscore must be uppercase letter");
    } catch (final IllegalArgumentException iae) {
    }
}