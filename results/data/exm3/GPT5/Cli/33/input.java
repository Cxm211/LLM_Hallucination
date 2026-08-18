// buggy function
    public void printWrapped(PrintWriter pw, int width, int nextLineTabStop, String text)
    {
        StringBuffer sb = new StringBuffer(text.length());

        renderWrappedText(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }

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
            nextLineTabStop = 1;
        }

        // all following lines must be padded with nextLineTabStop space characters
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
            
            if ((text.length() > width) && (pos == nextLineTabStop - 1))
            {
                pos = width;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }

// trigger testcase
// org/apache/commons/cli/HelpFormatterTest.java::testIndentedHeaderAndFooter
public void testIndentedHeaderAndFooter()
    {
        // related to CLI-207
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        String header = "  Header1\n  Header2";
        String footer = "  Footer1\n  Footer2";
        StringWriter out = new StringWriter();
        formatter.printHelp(new PrintWriter(out), 80, "foobar", header, options, 2, 2, footer, true);

        assertEquals(
                "usage: foobar" + EOL +
                "  Header1" + EOL +
                "  Header2" + EOL +
                "" + EOL +
                "  Footer1" + EOL +
                "  Footer2" + EOL
                , out.toString());
    }
