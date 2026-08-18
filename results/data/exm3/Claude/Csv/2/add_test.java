// org/apache/commons/csv/CSVRecordTest.java
public void testGetStringIndexOutOfBoundsNegative() {
    header.put("negative", Integer.valueOf(-1));
    assertNull(recordWithHeader.get("negative"));
}