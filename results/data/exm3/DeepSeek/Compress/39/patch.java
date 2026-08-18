public static String sanitize(String s) {
    final char[] chars = s.toCharArray();
    final int len = chars.length;
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i++) {
        final char c = chars[i];
        if (!Character.isISOControl(c)) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (block != null && block != Character.UnicodeBlock.SPECIALS) {
                sb.append(c);
                continue;
            }
        }
        sb.append('?');
    }
    String result = sb.toString();
    final int MAX_LENGTH = 255;
    if (result.length() > MAX_LENGTH) {
        result = result.substring(0, MAX_LENGTH - 3) + "...";
    }
    return result;
}