// org/apache/commons/csv/CSVFormatTest.java
public void testDuplicateHeaderElementsMessage() {
        try {
            CSVFormat.DEFAULT.withHeader("A", "A").validate();
            org.junit.Assert.fail("Expected IllegalStateException for duplicate header names");
        } catch (IllegalStateException ex) {
            org.junit.Assert.assertTrue(ex.getMessage().contains("The header contains duplicate names"));
        }
    }