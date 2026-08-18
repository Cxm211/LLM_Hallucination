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

        // all following lines must be padded with nextLineTabStop space 
        // characters
        final String padding = createPadding(nextLineTabStop);

        while (true)
        {
            String remainder = text.substring(pos);
            // Handle existing newlines by skipping them (the method already added a newline)
            if (remainder.startsWith("\r\n"))
            {
                remainder = remainder.substring(2);
            }
            else if (remainder.startsWith("\n"))
            {
                remainder = remainder.substring(1);
            }
            text = padding + remainder.trim();
            pos = findWrapPos(text, width, nextLineTabStop);

            if (pos == -1)
            {
                sb.append(text);

                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }