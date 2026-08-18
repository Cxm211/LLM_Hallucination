// org/apache/commons/codec/StringEncoderAbstractTest.java
public void testSoundexCleanLocaleIndependence() throws Exception {
    org.apache.commons.codec.language.Soundex soundex = new org.apache.commons.codec.language.Soundex();
    String[] data = {"i", "iPod", "test", "I"};
    java.util.Locale orig = java.util.Locale.getDefault();
    java.util.Locale[] locales = { java.util.Locale.ENGLISH, new java.util.Locale("tr"), java.util.Locale.getDefault() };
    try {
        for (String str : data) {
            String ref = null;
            for (int j = 0; j < locales.length; j++) {
                java.util.Locale.setDefault(locales[j]);
                if (j == 0) {
                    ref = soundex.encode(str);
                } else {
                    String cur = soundex.encode(str);
                    org.junit.Assert.assertEquals(java.util.Locale.getDefault().toString() + ": for input " + str, ref, cur);
                }
            }
        }
    } finally {
        java.util.Locale.setDefault(orig);
    }
}
