// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
@Test
public void testEqualsAdditional1() {
    CharSequence sb1 = new StringBuilder("test");
    CharSequence sb2 = new StringBuilder("test");
    assertTrue(StringUtils.equals(sb1, sb2));
}