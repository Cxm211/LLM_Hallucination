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
            // Skip any newline at the current position to preserve leading spaces
            int newPos = pos;
            if (newPos < text.length() && text.charAt(newPos) == '\n') {
                newPos++;
            }
            text = padding + text.substring(newPos);
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