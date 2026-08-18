// buggy code
    protected StringBuffer renderWrappedText(StringBuffer sb, int width, 
                                             int nextLineTabStop, String text)
    {
        int pos = findWrapPos(text, width, 0);  // 计算第一行的换行位置：找出在 width 限制下，应该在哪个位置断行

        if (pos == -1) //pos == -1 → 整段文本长度小于 width
        {
            sb.append(rtrim(text));

            return sb;
        }
        sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine); //截取 [0, pos) → 第一行内容

        // all following lines must be padded with nextLineTabStop space 
        // characters
        final String padding = createPadding(nextLineTabStop);

        while (true)
        {
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, nextLineTabStop);

            if (pos == -1)
            {
                sb.append(text);

                return sb;
            }

            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }

