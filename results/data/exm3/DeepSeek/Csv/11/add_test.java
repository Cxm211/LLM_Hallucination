// org/apache/commons/csv/CSVParserTest.java
@Test
    public void testIgnoreEmptyHeadersExplicit() throws Exception {
        final Reader in = new StringReader("1,2,3");
        CSVParser parser = CSVFormat.DEFAULT
            .withHeader("a", "", "c")
            .withIgnoreEmptyHeaders(true)
            .parse(in);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertFalse(headerMap.containsKey(""));
        assertEquals(2, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("a"));
        assertEquals(Integer.valueOf(2), headerMap.get("c"));
    }
