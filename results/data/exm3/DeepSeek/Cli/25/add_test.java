// org/apache/commons/cli/bug/BugCLI162Test.java
public void testRenderWrappedTextIndent() throws Exception {
    HelpFormatter formatter = new HelpFormatter();
    java.lang.reflect.Method m = HelpFormatter.class.getDeclaredMethod("renderWrappedText", StringBuffer.class, int.class, int.class, String.class);
    m.setAccessible(true);
    StringBuffer sb = new StringBuffer();
    m.invoke(formatter, sb, 10, 5, "123456789012345");
    String expected = "1234567890" + formatter.getNewLine() + "     1234" + formatter.getNewLine() + "     5";
    assertEquals(expected, sb.toString());
}
