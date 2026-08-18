// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintWrappedStartingNewline()
      throws Exception
   {
      StringBuffer sb = new StringBuffer();
      HelpFormatter hf = new HelpFormatter();

      String text = hf.getNewLine() + "Hello world.";
      String expected = text;
      hf.renderWrappedText(sb, 100, 0, text);
      assertEquals("starting newline", expected, sb.toString());
   }
