static float toJavaVersionInt(String version) {
        if (version == null || version.isEmpty()) {
            return 0f;
        }
        return (float) toVersionInt(toJavaVersionIntArray(version, JAVA_VERSION_TRIM_SIZE));
    }