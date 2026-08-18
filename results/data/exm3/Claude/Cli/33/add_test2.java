// org/apache/commons/cli/HelpFormatterTest.java
public void testRenderWrappedTextMultipleLongWordsWithIndent()
{
    HelpFormatter formatter = new HelpFormatter();
    StringBuffer sb = new StringBuffer();
    String text = "first verylongwordone verylongwordtwo end";
    formatter.renderWrappedText(sb, 20, 5, text);
    String expected = "first" + EOL + "     verylongwordone" + EOL + "     verylongwordtwo" + EOL + "     end" + EOL;
    assertEquals(expected, sb.toString());
}