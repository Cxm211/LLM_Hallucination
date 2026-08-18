// ===== FIXED org.apache.commons.lang.StringEscapeUtils :: escapeJava(String) [lines 85-87] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-46-fixed/src/java/org/apache/commons/lang/StringEscapeUtils.java =====
    public static String escapeJava(String str) {
        return escapeJavaStyleString(str, false, false);
    }

// ===== FIXED org.apache.commons.lang.StringEscapeUtils :: escapeJava(Writer, String) [lines 101-103] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-46-fixed/src/java/org/apache/commons/lang/StringEscapeUtils.java =====
    public static void escapeJava(Writer out, String str) throws IOException {
        escapeJavaStyleString(out, str, false, false);
    }

// ===== FIXED org.apache.commons.lang.StringEscapeUtils :: escapeJavaScript(String) [lines 126-128] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-46-fixed/src/java/org/apache/commons/lang/StringEscapeUtils.java =====
    public static String escapeJavaScript(String str) {
        return escapeJavaStyleString(str, true, true);
    }

// ===== FIXED org.apache.commons.lang.StringEscapeUtils :: escapeJavaScript(Writer, String) [lines 142-144] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-46-fixed/src/java/org/apache/commons/lang/StringEscapeUtils.java =====
    public static void escapeJavaScript(Writer out, String str) throws IOException {
        escapeJavaStyleString(out, str, true, true);
    }

// ===== FIXED org.apache.commons.lang.StringEscapeUtils :: escapeJavaStyleString(String, boolean) [lines 154-167] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-46-fixed/src/java/org/apache/commons/lang/StringEscapeUtils.java =====
    private static String escapeJavaStyleString(String str, boolean escapeSingleQuotes, boolean escapeForwardSlash) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(str.length() * 2);
            escapeJavaStyleString(writer, str, escapeSingleQuotes, escapeForwardSlash);
            return writer.toString();
        } catch (IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            ioe.printStackTrace();
            return null;
        }
    }

// ===== FIXED org.apache.commons.lang.StringEscapeUtils :: escapeJavaStyleString(Writer, String, boolean) [lines 154-167] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-46-fixed/src/java/org/apache/commons/lang/StringEscapeUtils.java =====
    private static String escapeJavaStyleString(String str, boolean escapeSingleQuotes, boolean escapeForwardSlash) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(str.length() * 2);
            escapeJavaStyleString(writer, str, escapeSingleQuotes, escapeForwardSlash);
            return writer.toString();
        } catch (IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            ioe.printStackTrace();
            return null;
        }
    }
