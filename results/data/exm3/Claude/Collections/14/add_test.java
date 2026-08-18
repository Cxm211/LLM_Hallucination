// org/apache/commons/collections/map/TestCaseInsensitiveMap.java
public void testTurkishLocaleSpecificCases() {
    Locale orig = Locale.getDefault();
    try {
        Locale.setDefault(new Locale("tr"));
        CaseInsensitiveMap map = new CaseInsensitiveMap();
        
        // Turkish-specific lowercase conversion: 'I' -> 'ı' (dotless i)
        // In English: 'I' -> 'i' (regular i)
        // The map should use English locale for consistency
        map.put("I", "capital_I");
        
        // Should retrieve with lowercase 'i' (English conversion)
        assertEquals("Should find 'I' with lowercase 'i'", "capital_I", map.get("i"));
        
        // Turkish uppercase 'İ' (capital i with dot)
        map.put("\u0130", "capital_I_with_dot");
        
        // Should retrieve with lowercase (English conversion of İ)
        assertEquals("Should find capital İ with lowercase", "capital_I_with_dot", map.get("\u0130".toLowerCase(Locale.ENGLISH)));
    } finally {
        Locale.setDefault(orig);
    }
}