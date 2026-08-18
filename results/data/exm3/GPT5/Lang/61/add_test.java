// org/apache/commons/lang/text/StrBuilderTest.java::testIndexOfBoundary
public void testIndexOfBoundary() {
        StrBuilder sb = new StrBuilder(11);
        sb.append("onetwothree");
        assertEquals(6, sb.indexOf("three"));
    }