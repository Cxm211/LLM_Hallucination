// buggy function
    <M extends Map<String, String>> M putIn(final M map) {
        for (final Entry<String, Integer> entry : mapping.entrySet()) {
            final int col = entry.getValue().intValue();
                map.put(entry.getKey(), values[col]);
        }
        return map;
    }

// trigger testcase
// org/apache/commons/csv/CSVRecordTest.java::testToMapWithShortRecord
@Test
    public void testToMapWithShortRecord() throws Exception {
       final CSVParser parser =  CSVParser.parse("a,b", CSVFormat.DEFAULT.withHeader("A", "B", "C"));
       final CSVRecord shortRec = parser.iterator().next();
       shortRec.toMap();
    }
