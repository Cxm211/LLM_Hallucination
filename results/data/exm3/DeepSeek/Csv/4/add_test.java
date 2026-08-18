// org/apache/commons/csv/CSVParserTest.java
@Test
    public void testNoHeaderMapWithTDF() throws Exception {
        final CSVParser parser = CSVParser.parse("a\tb\tc\n1\t2\t3", CSVFormat.TDF);
        Assert.assertNull(parser.getHeaderMap());
    }
