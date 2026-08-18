    <M extends Map<String, String>> M putIn(final M map) {
        for (final Entry<String, Integer> entry : mapping.entrySet()) {
            final int col = entry.getValue().intValue();
            if (col < values.length) {
                map.put(entry.getKey(), values[col]);
            }
        }
        return map;
    }

// trigger testcase
@Test
    public void testToMapWithNoHeader() throws Exception {
       final CSVParser parser =  CSVParser.parse("a,b", CSVFormat.newFormat(','));
       final CSVRecord shortRec = parser.iterator().next();
       Map<String, String> map = shortRec.toMap();
       assertNotNull("Map is not null.", map);
       assertTrue("Map is empty.", map.isEmpty());
    }
