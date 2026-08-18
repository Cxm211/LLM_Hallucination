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
            String remainder = text.substring(pos);
            if (remainder.length() > 0 && remainder.charAt(0) == '\n')
            {
                // preserve leading spaces after an explicit newline
                remainder = remainder.substring(1);
                text = padding + remainder;
            }
            else
            {
                text = padding + remainder.trim();
            }
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