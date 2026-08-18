// ===== FIXED org.apache.commons.lang3.SystemUtils :: toJavaVersionInt(String) [lines 1672-1674] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-29-fixed/src/main/java/org/apache/commons/lang3/SystemUtils.java =====
    static int toJavaVersionInt(String version) {
        return toVersionInt(toJavaVersionIntArray(version, JAVA_VERSION_TRIM_SIZE));
    }
