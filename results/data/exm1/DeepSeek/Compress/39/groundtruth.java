// ===== FIXED org.apache.commons.compress.utils.ArchiveUtils :: sanitize(String) [lines 273-295] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-39-fixed/src/main/java/org/apache/commons/compress/utils/ArchiveUtils.java =====
    public static String sanitize(String s) {
        final char[] cs = s.toCharArray();
        final char[] chars = cs.length <= MAX_SANITIZED_NAME_LENGTH ? cs : Arrays.copyOf(cs, MAX_SANITIZED_NAME_LENGTH);
        if (cs.length > MAX_SANITIZED_NAME_LENGTH) {
            for (int i = MAX_SANITIZED_NAME_LENGTH - 3; i < MAX_SANITIZED_NAME_LENGTH; i++) {
                chars[i] = '.';
            }
        }
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
        return sb.toString();
    }
