// buggy function
    protected StringBuffer renderWrappedText(StringBuffer sb, int width, 
                                             int nextLineTabStop, String text)
    {
        int pos = findWrapPos(text, width, 0);

        if (pos == -1)
        {
            sb.append(rtrim(text));

            return sb;
        }
        sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);

        if (nextLineTabStop >= width)
        {
            // stops infinite loop happening
            nextLineTabStop = width - 1;
        }

        // all following lines must be padded with nextLineTabStop space 
        // characters
        final String padding = createPadding(nextLineTabStop);

        while (true)
        {
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, 0);

            if (pos == -1)
            {
                sb.append(text);

                return sb;
            }
            
            if ( (text.length() > width) && (pos == nextLineTabStop - 1) ) 
            {
                pos = width;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }

// trigger testcase
// org/apache/commons/cli/bug/BugCLI162Test.java::testLongLineChunkingIndentIgnored
public void testLongLineChunkingIndentIgnored() throws ParseException, IOException {
        Options options = new Options();
        options.addOption("x", "extralongarg", false, "This description is Long." );
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        formatter.printHelp(new PrintWriter(sw), 22, this.getClass().getName(), "Header", options, 0, 5, "Footer");
        System.err.println(sw.toString());
        String expected = "usage:\n" +
                          "       org.apache.comm\n" +
                          "       ons.cli.bug.Bug\n" +
                          "       CLI162Test\n" +
                          "Header\n" +
                          "-x,--extralongarg\n" +
                          " This description is\n" +
                          " Long.\n" +
                          "Footer\n";
        assertEquals( "Long arguments did not split as expected", expected, sw.toString() );
    }
