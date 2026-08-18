// org/apache/commons/lang/LocaleUtilsTest.java
public void testLang331() {
        try {
            LocaleUtils.toLocale("fr__");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
