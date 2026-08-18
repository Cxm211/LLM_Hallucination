// org/apache/commons/cli/HelpFormatterTest.java
public void testRenderWrappedTextLongWordWithIndent()
{
    HelpFormatter formatter = new HelpFormatter();
    StringBuffer sb = new StringBuffer();
    String text = "verylongwordthatcannotfit";
    formatter.renderWrappedText(sb, 20, 5, text);
    String expected = "verylongwordthatcannotfit" + EOL;
    assertEquals(expected, sb.toString());
}