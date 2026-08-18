// org/apache/commons/lang/StringUtilsEqualsIndexOfTest.java
public void testContainsIgnoreCase_Additional() {
    Locale original = Locale.getDefault();
    try {
        Locale.setDefault(new Locale("tr"));
        assertTrue(StringUtils.containsIgnoreCase("internet", "INTERNET"));
        assertTrue(StringUtils.containsIgnoreCase("Istanbul", "istanbul"));
        assertFalse(StringUtils.containsIgnoreCase("\u0130", "i"));
        assertFalse(StringUtils.containsIgnoreCase("i", "\u0130"));
        assertFalse(StringUtils.containsIgnoreCase("\u0130stanbul", "istanbul"));
        assertFalse(StringUtils.containsIgnoreCase("\u00DF", "SS"));
        assertFalse(StringUtils.containsIgnoreCase("SS", "\u00DF"));
        assertFalse(StringUtils.containsIgnoreCase("stra\u00DFe", "STRASSE"));
        assertFalse(StringUtils.containsIgnoreCase("STRASSE", "stra\u00DFe"));
    } finally {
        Locale.setDefault(original);
    }
}
