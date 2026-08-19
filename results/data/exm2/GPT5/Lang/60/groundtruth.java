// ===== FIXED org.apache.commons.lang.text.StrBuilder :: contains(char) [lines 1671-1679] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-60-fixed/src/java/org/apache/commons/lang/text/StrBuilder.java =====
    public boolean contains(char ch) {
        char[] thisBuf = buffer;
        for (int i = 0; i < this.size; i++) {
            if (thisBuf[i] == ch) {
                return true;
            }
        }
        return false;
    }

// ===== FIXED org.apache.commons.lang.text.StrBuilder :: indexOf(char, int) [lines 1724-1736] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-60-fixed/src/java/org/apache/commons/lang/text/StrBuilder.java =====
    public int indexOf(char ch, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (startIndex >= size) {
            return -1;
        }
        char[] thisBuf = buffer;
        for (int i = startIndex; i < size; i++) {
            if (thisBuf[i] == ch) {
                return i;
            }
        }
        return -1;
    }
