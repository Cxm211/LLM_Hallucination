// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddWithNonNullArray(){
        String[] arr = new String[]{"a"};
        String[] res = ArrayUtils.add(arr, "b");
        assertEquals(2, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
    }