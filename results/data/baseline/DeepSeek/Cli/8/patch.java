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

        String remainder = text.substring(pos).trim();
        if (remainder.isEmpty())
        {
            return sb;
        }

        final String padding = createPadding(nextLineTabStop);
        text = padding + remainder;

        while (true)
        {
            pos = findWrapPos(text, width, nextLineTabStop);

            if (pos == -1)
            {
                sb.append(text);
                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);

            remainder = text.substring(pos).trim();
            if (remainder.isEmpty())
            {
                return sb;
            }
            text = padding + remainder;
        }
    }