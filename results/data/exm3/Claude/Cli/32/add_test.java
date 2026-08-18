// org/apache/commons/cli/HelpFormatterTest.java
public void testFindWrapPosWithNewline() throws Exception
{
    HelpFormatter hf = new HelpFormatter();
    
    // Test newline within width
    String text = "Hello\nWorld";
    assertEquals("wrap at newline", 6, hf.findWrapPos(text, 10, 0));
    
    // Test newline exactly at width boundary
    text = "Hello\nWorld";
    assertEquals("wrap at newline boundary", 6, hf.findWrapPos(text, 5, 0));
    
    // Test tab within width
    text = "Hello\tWorld";
    assertEquals("wrap at tab", 6, hf.findWrapPos(text, 10, 0));
}