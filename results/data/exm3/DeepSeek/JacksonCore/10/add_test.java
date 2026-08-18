// com/fasterxml/jackson/core/sym/TestSymbolTables.java
public void testHashZero() throws Exception {
    int seed = 12345;
    ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(seed);
    int hash = sym.calcHash(seed);
    // In buggy version, hash will be 0; fixed version will be non-zero
    assertNotEquals(0, hash);
}
