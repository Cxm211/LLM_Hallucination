// org/apache/commons/lang3/RandomStringUtilsTest.java
public void testRandomWithNonEmptyCharArray() {
    long seed = System.currentTimeMillis();
    String result = RandomStringUtils.random(5, 0, 0, false, false, new char[]{'x', 'y', 'z'}, new Random(seed));
    assertEquals(5, result.length());
    for (char c : result.toCharArray()) {
        assertTrue(c == 'x' || c == 'y' || c == 'z');
    }
}