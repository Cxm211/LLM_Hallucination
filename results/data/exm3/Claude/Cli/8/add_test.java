// org/apache/commons/cli/HelpFormatterTest.java
public void testPrintWrappedLongWordWithPadding()
      throws Exception
   {
      StringBuffer sb = new StringBuffer();
      HelpFormatter hf = new HelpFormatter();

      String text = "short verylongwordthatdoesnotfitonline end";
      String expected = "short" + hf.getNewLine() +
                        "    verylongwordthatdoesnotfitonline" + hf.getNewLine() +
                        "    end";
      hf.renderWrappedText(sb, 20, 4, text);
      System.out.println("EXPECTED:");
      System.out.println(expected.replace("\n", "\\n\n"));

      System.out.println("ACTUAL:");
      System.out.println(sb.toString().replace("\n", "\\n\n"));

      assertEquals("long word with padding", expected, sb.toString());
   }