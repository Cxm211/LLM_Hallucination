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
                sb.append(text.substring(0, width)).append(defaultNewLine);
                text = text.substring(width);
                pos = nextLineTabStop;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }