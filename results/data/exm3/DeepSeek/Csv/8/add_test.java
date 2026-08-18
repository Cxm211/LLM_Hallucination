// org/apache/commons/csv/CSVFormatTest.java
@Test
    public void testDuplicateHeaderElementsCaseInsensitive() {
        try {
            CSVFormat.DEFAULT.withHeader("a", "A").validate();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("duplicate"));
        }
    }
