// org/apache/commons/codec/binary/StringUtilsTest.java
@Test
public void testEqualsCS4() {
    Assert.assertFalse(StringUtils.equals("abc", "abcdef"));
    Assert.assertFalse(StringUtils.equals("abcdef", "abc"));
    Assert.assertFalse(StringUtils.equals(new StringBuilder("xyz"), new StringBuilder("xyzabc")));
    Assert.assertFalse(StringUtils.equals(new StringBuilder("xyzabc"), new StringBuilder("xyz")));
}