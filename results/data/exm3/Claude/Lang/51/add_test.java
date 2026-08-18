// org/apache/commons/lang/BooleanUtilsTest.java
public void test_toBoolean_String_EdgeCases() {
    assertEquals(false, BooleanUtils.toBoolean("o"));
    assertEquals(false, BooleanUtils.toBoolean("y"));
    assertEquals(false, BooleanUtils.toBoolean("Y"));
    assertEquals(false, BooleanUtils.toBoolean("t"));
    assertEquals(false, BooleanUtils.toBoolean("T"));
    assertEquals(false, BooleanUtils.toBoolean("ye"));
    assertEquals(false, BooleanUtils.toBoolean("Ye"));
    assertEquals(false, BooleanUtils.toBoolean("YE"));
    assertEquals(false, BooleanUtils.toBoolean("yE"));
    assertEquals(false, BooleanUtils.toBoolean("tr"));
    assertEquals(false, BooleanUtils.toBoolean("Tr"));
    assertEquals(false, BooleanUtils.toBoolean("TR"));
    assertEquals(false, BooleanUtils.toBoolean("tR"));
    assertEquals(false, BooleanUtils.toBoolean("abc"));
    assertEquals(false, BooleanUtils.toBoolean("xyz"));
    assertEquals(false, BooleanUtils.toBoolean("abcd"));
    assertEquals(false, BooleanUtils.toBoolean("xyzw"));
    assertEquals(false, BooleanUtils.toBoolean("longer string"));
}