// org/apache/commons/csv/CSVFormatTest.java
@Test
    public void testDuplicateHeaderWithNoEscape() {
        try {
            CSVFormat format = CSVFormat.newFormat(',').withQuoteChar(null).withEscape(null).withHeader("A", "A");
            format.validate();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("duplicate"));
        }
    }
