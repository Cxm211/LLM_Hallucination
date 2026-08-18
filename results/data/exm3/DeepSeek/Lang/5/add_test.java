// org/apache/commons/lang3/LocaleUtilsTest.java
@Test
public void testUnderscoreWithNumericVariant() {
    assertValidToLocale("_US_123", "", "US", "123");
}
