// org/apache/commons/cli/HelpFormatterTest.java
public void testFindWrapPosNewlineTabCr() {
    HelpFormatter hf = new HelpFormatter();
    // newline with startPos > 0
    String text = "abc\\ndef";
    assertEquals("newline wrap", 4, hf.findWrapPos(text, 2, 1));
    // tab with startPos > 0
    text = "abc\\tdef";
    assertEquals("tab wrap", 4, hf.findWrapPos(text, 2, 1));
    // carriage return with startPos > 0
    text = "abc\\rdef";
    assertEquals("carriage return wrap", 4, hf.findWrapPos(text, 2, 1));
}
