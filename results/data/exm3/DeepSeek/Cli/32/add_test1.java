// org/apache/commons/cli/HelpFormatterTest.java
public void testFindWrapPosNoWhitespace() {
    HelpFormatter hf = new HelpFormatter();
    String text = "abcdef";
    // width 3, startPos 0, should cut at 3
    assertEquals("no whitespace cut 1", 3, hf.findWrapPos(text, 3, 0));
    // startPos 2, width 2, should cut at 4
    assertEquals("no whitespace cut 2", 4, hf.findWrapPos(text, 2, 2));
    // startPos 5, width 1, beyond end? returns -1
    assertEquals("no whitespace beyond end", -1, hf.findWrapPos(text, 1, 5));
}
