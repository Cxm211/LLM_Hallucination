// org/apache/commons/lang3/StringUtilsTest.java
public void testJoin_ArrayChar_NullToString() {
        Object obj = new Object() {
            @Override
            public String toString() {
                return null;
            }
        };
        Object[] array = {obj};
        String result = StringUtils.join(array, ',', 0, 1);
        assertEquals("null", result);
    }
