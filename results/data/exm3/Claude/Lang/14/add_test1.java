// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
@Test
public void testEqualsAdditional2() {
    CharSequence sb1 = new StringBuilder("test");
    CharSequence sb2 = new StringBuilder("Test");
    assertFalse(StringUtils.equals(sb1, sb2));
}