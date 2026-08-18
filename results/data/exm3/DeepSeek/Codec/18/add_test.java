// org/apache/commons/codec/binary/StringUtilsTest.java
@Test
    public void testEqualsCS3() {
        // Test with empty vs non-empty CharSequences
        Assert.assertFalse(StringUtils.equals(new StringBuilder(""), new StringBuilder("a")));
        Assert.assertFalse(StringUtils.equals(new StringBuilder("a"), new StringBuilder("")));
        // Test with different CharSequence types (StringBuffer) and length difference
        Assert.assertFalse(StringUtils.equals(new StringBuffer("xyz"), new StringBuilder("xy")));
        Assert.assertFalse(StringUtils.equals(new StringBuilder("xy"), new StringBuffer("xyz")));
    }
