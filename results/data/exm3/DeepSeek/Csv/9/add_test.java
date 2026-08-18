// org/apache/commons/csv/CSVRecordTest.java
@Test
    public void testPutInWithNoHeader() throws Exception {
        final CSVParser parser = CSVParser.parse("a,b", CSVFormat.newFormat(','));
        final CSVRecord record = parser.iterator().next();
        Map<String, String> map = new HashMap<>();
        map.put("test", "value");
        record.putIn(map);
        assertEquals(1, map.size());
        assertEquals("value", map.get("test"));
    }
