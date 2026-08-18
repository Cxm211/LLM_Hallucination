// org/apache/commons/collections/map/TestCaseInsensitiveMap.java
public void testGreekSigmaVariants() {
    CaseInsensitiveMap map = new CaseInsensitiveMap();
    
    // Greek capital sigma: Σ (U+03A3)
    // Greek small sigma: σ (U+03C3)
    // Greek small final sigma: ς (U+03C2)
    // All three should map to the same key
    
    map.put("\u03A3", "capital_sigma");
    
    // Should be able to retrieve with lowercase sigma
    assertEquals("Should find Σ with σ", "capital_sigma", map.get("\u03C3"));
    
    // Should be able to retrieve with final sigma
    assertEquals("Should find Σ with ς", "capital_sigma", map.get("\u03C2"));
    
    // Test reverse: put with lowercase, get with uppercase
    map.clear();
    map.put("\u03C3", "lowercase_sigma");
    assertEquals("Should find σ with Σ", "lowercase_sigma", map.get("\u03A3"));
    
    // Put with final sigma, get with regular sigma
    map.clear();
    map.put("\u03C2", "final_sigma");
    assertEquals("Should find ς with σ", "final_sigma", map.get("\u03C3"));
}