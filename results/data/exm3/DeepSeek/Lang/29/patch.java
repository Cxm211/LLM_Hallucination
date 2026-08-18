    static float toJavaVersionInt(String version) {
        if (version == null) {
            return 0f;
        }
        String[] parts = version.split("[^0-9]+");
        int major = 0;
        int minor = 0;
        int patch = 0;
        int count = 0;
        for (String part : parts) {
            if (!part.isEmpty()) {
                int num = Integer.parseInt(part);
                if (count == 0) {
                    major = num;
                } else if (count == 1) {
                    minor = num;
                } else if (count == 2) {
                    patch = num;
                } else {
                    break;
                }
                count++;
            }
        }
        if (count == 0) {
            return 0f;
        }
        return (float) (major * 100 + minor * 10 + patch);
    }