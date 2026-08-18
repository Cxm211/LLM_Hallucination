// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
@Test
public void testEqualsAdditional3() {
    CharSequence sb1 = new StringBuilder("test");
    CharSequence sb2 = new StringBuilder("testing");
    assertFalse(StringUtils.equals(sb1, sb2));
}