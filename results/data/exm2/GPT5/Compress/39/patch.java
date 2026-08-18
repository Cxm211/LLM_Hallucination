public static String sanitize(String s) {
    final int maxLen = 255;
    final int ellipsisLen = 3;
    final char[] chars = s.toCharArray();
    final int len = chars.length;
    final StringBuilder sb = new StringBuilder(Math.min(len, maxLen));
    for (int i = 0; i < len; i++) {
        final char c = chars[i];
        char out;
        if (!Character.isISOControl(c)) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (block != null && block != Character.UnicodeBlock.SPECIALS) {
                out = c;
            } else {
                out = '?';
            }
        } else {
            out = '?';
        }
        if (i < len - 1 && sb.length() >= maxLen - ellipsisLen) {
            sb.append("...");
            break;
        }
        if (sb.length() < maxLen) {
            sb.append(out);
        }
    }
    return sb.toString();
}