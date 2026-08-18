// org/apache/commons/jxpath/ri/model/JXPath151Test.java
public void testMapNullValueLength() {
    Object value = context.getValue("map/nullValue");
    if (value == null) {
        Pointer pointer = context.getPointer("map/nullValue");
        int length = pointer.getLength();
        assertEquals(1, length);
    }
    
    Pointer pointer = context.getPointer("map/a");
    int length = pointer.getLength();
    assertTrue("Length should be >= 1 for non-null values", length >= 1);
}