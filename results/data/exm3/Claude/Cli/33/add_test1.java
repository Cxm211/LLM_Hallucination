// org/apache/commons/cli/HelpFormatterTest.java
public void testRenderWrappedTextLongWordAfterWrapWithIndent()
{
    HelpFormatter formatter = new HelpFormatter();
    StringBuffer sb = new StringBuffer();
    String text = "short verylongwordthatexceedswidth";
    formatter.renderWrappedText(sb, 20, 5, text);
    String expected = "short" + EOL + "     verylongwordthatexceedswidth" + EOL;
    assertEquals(expected, sb.toString());
}