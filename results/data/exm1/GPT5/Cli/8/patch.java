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
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, nextLineTabStop);

            if (pos == -1)
            {
                if (nextLineTabStop > 0)
                {
                    String nl = defaultNewLine;
                    int idx = 0;
                    StringBuilder tmp = new StringBuilder();
                    while (true)
                    {
                        int n = text.indexOf(nl, idx);
                        if (n < 0)
                        {
                            tmp.append(text.substring(idx));
                            break;
                        }
                        tmp.append(text, idx, n + nl.length()).append(padding);
                        idx = n + nl.length();
                    }
                    sb.append(tmp.toString());
                }
                else
                {
                    sb.append(text);
                }

                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }