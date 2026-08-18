protected StringBuffer renderWrappedText(StringBuffer sb, int width, int nextLineTabStop, String text) {
    String[] lines = text.split("\n", -1);
    for (int i = 0; i < lines.length; i++) {
        if (i > 0) {
            sb.append(defaultNewLine);
        }
        renderWrappedLine(sb, width, nextLineTabStop, lines[i]);
    }
    return sb;
}

private void renderWrappedLine(StringBuffer sb, int width, int nextLineTabStop, String text) {
    int pos = findWrapPos(text, width, 0);
    if (pos == -1) {
        sb.append(rtrim(text));
        return;
    }
    sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
    final String padding = createPadding(nextLineTabStop);
    while (true) {
        int lastPos = pos;
        text = padding + text.substring(pos).trim();
        pos = findWrapPos(text, width, 0);
        if (pos == -1) {
            sb.append(text);
            return;
        }
        if (pos == lastPos) {
            pos = width;
            if (pos >= text.length()) {
                sb.append(text);
                return;
            }
        }
        sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
    }
}