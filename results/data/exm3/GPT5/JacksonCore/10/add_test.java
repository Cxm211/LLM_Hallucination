// com/fasterxml/jackson/core/sym/TestSymbolTables.java::testSyntheticWithBytesNew
public void testSpilloverVsLongNamesBoundary() throws IOException
    {
        // Use many long names to populate long-names area, plus enough entries to push spillover
        final int SEED = 13579;
        ByteQuadsCanonicalizer symbols =
                ByteQuadsCanonicalizer.createRoot(SEED).makeChild(JsonFactory.Feature.collectDefaults());

        final int COUNT = 8000;
        for (int i = 0; i < COUNT; ++i) {
            String id = "field_" + i + "_XXXXXXXXXXXXXXXXXXXXXXXXXXXX"; // long to ensure long-names area usage
            int[] quads = calcQuads(id.getBytes("UTF-8"));
            symbols.addName(id, quads, quads.length);
        }
        // Should add all without corruption
        assertEquals(COUNT, symbols.size());
    }