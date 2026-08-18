// org/apache/commons/csv/CSVParserTest.java
@Test
    public void testIgnoreEmptyHeadersAllEmpty() throws Exception {
        final Reader in = new StringReader(",,\n1,2,3");
        CSVParser parser = CSVFormat.DEFAULT
            .withHeader()
            .withIgnoreEmptyHeaders(true)
            .parse(in);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertTrue(headerMap.isEmpty());
    }
