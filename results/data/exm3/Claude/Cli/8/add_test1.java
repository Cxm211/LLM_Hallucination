// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintWrappedExactWidthWithPadding()
      throws Exception
   {
      StringBuffer sb = new StringBuffer();
      HelpFormatter hf = new HelpFormatter();

      String text = "word anotherverylongword";
      String expected = "word" + hf.getNewLine() +
                        "  anotherverylongword";
      hf.renderWrappedText(sb, 20, 2, text);
      assertEquals("exact width word with padding", expected, sb.toString());
   }