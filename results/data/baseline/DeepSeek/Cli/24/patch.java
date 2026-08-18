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
            throw new IllegalStateException("Total width is less than the width of the argument and indent " + 
                                            "- no room for the description");
        }

        // all following lines must be padded with nextLineTabStop space 
        // characters
        final String padding = createPadding(nextLineTabStop);

        String remaining = text.substring(pos).trim();

        while (true)
        {
            String line = padding + remaining;
            pos = findWrapPos(line, width, 0);

            if (pos == -1)
            {
                sb.append(line);

                return sb;
            }
            
            if ( (line.length() > width) && (pos == nextLineTabStop - 1) ) 
            {
                pos = width;
            }

            sb.append(rtrim(line.substring(0, pos))).append(defaultNewLine);
            remaining = line.substring(pos).trim();
        }
    }