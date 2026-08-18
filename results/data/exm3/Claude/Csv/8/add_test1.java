// org/apache/commons/csv/CSVFormatTest.java
@Test(expected = IllegalStateException.class)
public void testDuplicateHeaderElementsMultipleDuplicates() {
    CSVFormat.DEFAULT.withHeader("A", "B", "A", "B");
}