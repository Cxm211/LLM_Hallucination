// org/apache/commons/codec/StringEncoderAbstractTest.java
public void testCaverphoneLocaleIndependence() throws Exception {
    org.apache.commons.codec.language.Caverphone caverphone = new org.apache.commons.codec.language.Caverphone();
    String[] data = {"I", "i", "Igloo", "IRON", "In", "iS", "Is", "THIS"};
    java.util.Locale orig = java.util.Locale.getDefault();
    java.util.Locale[] locales = { java.util.Locale.ENGLISH, new java.util.Locale("tr"), java.util.Locale.getDefault() };
    try {
        for (String str : data) {
            String ref = null;
            for (int j = 0; j < locales.length; j++) {
                java.util.Locale.setDefault(locales[j]);
                if (j == 0) {
                    ref = caverphone.caverphone(str);
                } else {
                    String cur = caverphone.caverphone(str);
                    org.junit.Assert.assertEquals(java.util.Locale.getDefault().toString() + ": for input " + str, ref, cur);
                }
            }
        }
    } finally {
        java.util.Locale.setDefault(orig);
    }
}
