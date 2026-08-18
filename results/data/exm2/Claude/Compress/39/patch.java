public static String sanitize(String s) {
    final char[] chars = s.toCharArray();
    final int len = chars.length;
    final StringBuilder sb = new StringBuilder();
    final int maxLength = 254;
    for (int i = 0; i < len; i++) {
        final char c = chars[i];
        if (!Character.isISOControl(c)) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (block != null && block != Character.UnicodeBlock.SPECIALS) {
                sb.append(c);
                if (sb.length() >= maxLength) {
                    sb.append("...");
                    break;
                }
                continue;
            }
        }
        sb.append('?');
        if (sb.length() >= maxLength) {
            sb.append("...");
            break;
        }
    }
    return sb.toString();
}