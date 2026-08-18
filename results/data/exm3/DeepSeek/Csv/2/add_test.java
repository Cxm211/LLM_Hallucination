// org/apache/commons/csv/CSVRecordTest.java
public void testGetStringNegativeIndex() {
    Map<String, Integer> map = new HashMap<>();
    map.put("neg", Integer.valueOf(-1));
    String[] values = {"value0"};
    CSVRecord record = new CSVRecord(values, map, null, 0);
    assertNull(record.get("neg"));
}
