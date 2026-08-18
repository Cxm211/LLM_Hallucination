// org/apache/commons/lang3/RandomStringUtilsTest.java
public void testRandomWithSingleCharArray() {
    String result = RandomStringUtils.random(10, 0, 0, false, false, new char[]{'b'}, new Random(123));
    assertEquals("bbbbbbbbbb", result);
}