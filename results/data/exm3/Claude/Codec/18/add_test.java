// org/apache/commons/codec/binary/StringUtilsTest.java
@Test
public void testEqualsCS3() {
    Assert.assertTrue(StringUtils.equals("", ""));
    Assert.assertFalse(StringUtils.equals("", "a"));
    Assert.assertFalse(StringUtils.equals("a", ""));
    Assert.assertTrue(StringUtils.equals(new StringBuilder(""), new StringBuilder("")));
    Assert.assertFalse(StringUtils.equals(new StringBuilder(""), new StringBuilder("a")));
}