// org/apache/commons/cli/UtilTest.java
@Test
    public void testStripLeadingHyphensExtended() {
        assertNull(Util.stripLeadingHyphens(null));
        assertEquals("", Util.stripLeadingHyphens(""));
        assertEquals("", Util.stripLeadingHyphens("-"));
        assertEquals("", Util.stripLeadingHyphens("--"));
        assertEquals("foo", Util.stripLeadingHyphens("foo"));
        assertEquals("-test", Util.stripLeadingHyphens("---test"));
    }
