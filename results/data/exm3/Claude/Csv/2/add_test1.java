// org/apache/commons/csv/CSVRecordTest.java
public void testGetStringIndexAtBoundary() {
    int lastIndex = recordWithHeader.size();
    header.put("boundary", Integer.valueOf(lastIndex));
    assertNull(recordWithHeader.get("boundary"));
}