// buggy function
    public static String newStringIso8859_1(final byte[] bytes) {
        return new String(bytes, Charsets.ISO_8859_1);
    }

// trigger testcase
// org/apache/commons/codec/binary/StringUtilsTest.java::testNewStringNullInput_CODEC229
@Test
    public void testNewStringNullInput_CODEC229() {
        Assert.assertNull(StringUtils.newStringUtf8(null));
        Assert.assertNull(StringUtils.newStringIso8859_1(null));
        Assert.assertNull(StringUtils.newStringUsAscii(null));
        Assert.assertNull(StringUtils.newStringUtf16(null));
        Assert.assertNull(StringUtils.newStringUtf16Be(null));
        Assert.assertNull(StringUtils.newStringUtf16Le(null));
    }
