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
            // If the remainder starts with a newline, drop exactly one newline
            if (remainder.length() > 0 && remainder.charAt(0) == '\n')
            {
                remainder = remainder.substring(1);
            }
            // Remove leading spaces/tabs only (do not trim newlines)
            int start = 0;
            while (start < remainder.length())
            {
                char ch = remainder.charAt(start);
                if (ch == ' ' || ch == '\t')
                {
                    start++;
                }
                else
                {
                    break;
                }
            }
            remainder = remainder.substring(start);

            text = padding + remainder;
            pos = findWrapPos(text, width, nextLineTabStop);

            if (pos == -1)
            {
                sb.append(text);

                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }