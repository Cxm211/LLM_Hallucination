// com/fasterxml/jackson/core/sym/TestSymbolTables.java
public void testSpilloverBoundaryCondition() throws IOException
{
    ByteQuadsCanonicalizer symbols =
            ByteQuadsCanonicalizer.createRoot(12345).makeChild(JsonFactory.Feature.collectDefaults());

    final int COUNT = 15000;
    for (int i = 0; i < COUNT; ++i) {
        String id = "prefix_" + i + "_suffix";
        int[] quads = calcQuads(id.getBytes("UTF-8"));
        symbols.addName(id, quads, quads.length);
    }
    assertEquals(COUNT, symbols.size());
    assertTrue(symbols.spilloverCount() > 0);
}