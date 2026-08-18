// org/apache/commons/csv/CSVFormatTest.java
@Test
public void testNoDuplicateHeaderElements() {
    CSVFormat.DEFAULT.withHeader("A", "B", "C");
}