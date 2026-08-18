// org/apache/commons/cli/HelpFormatterTest.java::testPrintWrapped
public void testPrintWrappedLeadingSpacesAfterNewline() throws Exception {
      StringBuffer sb = new StringBuffer();
      HelpFormatter hf = new HelpFormatter();
      String nl = hf.getNewLine();

      String text = "aa aa" + nl + "   bb";

      // No padding: leading spaces after newline should be trimmed
      sb.setLength(0);
      hf.renderWrappedText(sb, 10, 0, text);
      assertEquals("no padding, ltrim after newline", "aa aa" + nl + "bb", sb.toString());

      // With padding: leading spaces after newline should be replaced by padding
      sb.setLength(0);
      hf.renderWrappedText(sb, 10, 4, text);
      assertEquals("with padding, ltrim and pad after newline", "aa aa" + nl + "    bb", sb.toString());
   }