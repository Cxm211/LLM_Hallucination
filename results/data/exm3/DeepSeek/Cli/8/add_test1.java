// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintWrappedPaddedLineWrap()
      throws Exception
   {
      StringBuffer sb = new StringBuffer();
      HelpFormatter hf = new HelpFormatter();

      String text = "First line" + hf.getNewLine() + "This is a long line that needs to wrap";
      String expected = "First line" + hf.getNewLine() + "    This is a long" + hf.getNewLine() + "    line that needs" + hf.getNewLine() + "    to wrap";
      hf.renderWrappedText(sb, 20, 4, text);
      assertEquals("padded line wrap", expected, sb.toString());
   }
