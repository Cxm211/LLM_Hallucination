// org/apache/commons/lang/text/StrBuilderTest.java::testIndexOfDoesNotScanBeyondSizeWithStartIndex
public void testIndexOfDoesNotScanBeyondSizeWithStartIndex() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.delete(3, 6); // leaves "abc" but buffer may still hold 'd','e','f' beyond size
        assertEquals(-1, sb.indexOf('e', 0));
        assertEquals(-1, sb.indexOf('e', 1));
    }