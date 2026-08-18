// org/apache/commons/csv/CSVFormatTest.java
@Test(expected = IllegalStateException.class)
public void testDuplicateHeaderElementsWithThreeElements() {
    CSVFormat.DEFAULT.withHeader("A", "B", "A");
}