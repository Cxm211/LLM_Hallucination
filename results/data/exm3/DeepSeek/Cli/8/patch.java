    protected StringBuffer renderWrappedText(StringBuffer sb, int width, 
                                             int nextLineTabStop, String text)
    {
        int pos = findWrapPos(text, width, 0);

        if (pos == -1)
        {
            sb.append(rtrim(text));

            return sb;
        }
        // Check if the wrap position is at a newline
        if (text.startsWith(defaultNewLine, pos))
        {
            // Output up to and including the newline
            sb.append(text.substring(0, pos + defaultNewLine.length()));
            // Remove the processed part including newline
            text = text.substring(pos + defaultNewLine.length());
            if (text.length() == 0) {
                return sb;
            }
        }
        else
        {
            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
            text = text.substring(pos);
        }

        // all following lines must be padded with nextLineTabStop space 
        // characters
        final String padding = createPadding(nextLineTabStop);

        while (true)
        {
            // Trim leading whitespace from the remaining text before adding padding
            text = padding + text.trim();
            pos = findWrapPos(text, width, nextLineTabStop);

            if (pos == -1)
            {
                sb.append(text);

                return sb;
            }

            // Check if the wrap position is at a newline
            if (text.startsWith(defaultNewLine, pos))
            {
                sb.append(text.substring(0, pos + defaultNewLine.length()));
                text = text.substring(pos + defaultNewLine.length());
                if (text.length() == 0) {
                    return sb;
                }
                continue;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
            text = text.substring(pos);
        }
    }