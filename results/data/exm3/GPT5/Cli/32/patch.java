protected int findWrapPos(String text, int width, int startPos)
    {
        int pos;
        
        // the line ends before the max wrap pos or a new line/tab char found within the width window
        if (((pos = text.indexOf('\n', startPos)) != -1 && (pos - startPos) <= width)
                || ((pos = text.indexOf('\t', startPos)) != -1 && (pos - startPos) <= width))
        {
            return pos + 1;
        }
        else if (startPos + width >= text.length())
        {
            return -1;
        }

        // look for the last whitespace character before startPos+width
        pos = Math.min(startPos + width, text.length() - 1);

        char c;

        while ((pos >= startPos) && ((c = text.charAt(pos)) != ' ')
                && (c != '\n') && (c != '\r') && (c != '\t'))
        {
            --pos;
        }

        // if we found it - just return (allowing whitespace exactly at startPos)
        if (pos >= startPos)
        {
            return pos;
        }
        
        // if we didn't find one, simply chop at startPos+width
        return startPos + width;
    }