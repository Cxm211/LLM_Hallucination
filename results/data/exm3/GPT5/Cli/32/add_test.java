// org/apache/commons/cli/HelpFormatterTest.java::testFindWrapPosNewlineWithinWidthFromOffset
public void testFindWrapPosNewlineWithinWidthFromOffset() throws Exception
    {
        HelpFormatter hf = new HelpFormatter();
        String text = "hello\nworld";
        // newline is within width from the offset; should wrap after the newline
        assertEquals("wrap pos with newline", 6, hf.findWrapPos(text, 3, 2));
    }