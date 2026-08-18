// org/apache/commons/csv/CSVRecordTest.java
@Test
    public void testToMapWithSingleValueAndThreeHeaders() throws Exception {
        final CSVParser parser = CSVParser.parse("a", CSVFormat.DEFAULT.withHeader("A", "B", "C"));
        final CSVRecord shortRec = parser.iterator().next();
        shortRec.toMap();
    }
