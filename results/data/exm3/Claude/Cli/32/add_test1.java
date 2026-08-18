// org/apache/commons/cli/HelpFormatterTest.java
public void testFindWrapPosEdgeCases() throws Exception
{
    HelpFormatter hf = new HelpFormatter();
    
    // Test exact boundary with space
    String text = "abc def";
    assertEquals("wrap at space", 3, hf.findWrapPos(text, 3, 0));
    
    // Test no whitespace found forward search
    text = "aaaaaaaaaa";
    assertEquals("no wrap found", -1, hf.findWrapPos(text, 5, 0));
    
    // Test startPos at exact char before end
    text = "test x";
    assertEquals("wrap near end", -1, hf.findWrapPos(text, 3, 5));
}