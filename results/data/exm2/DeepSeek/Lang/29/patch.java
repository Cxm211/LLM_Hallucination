    static float toJavaVersionInt(String version) {
        if (version == null || version.isEmpty()) {
            return 0.0f;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(version);
        int[] numbers = new int[3];
        int i = 0;
        while (m.find() && i < 3) {
            numbers[i] = Integer.parseInt(m.group());
            i++;
        }
        if (i == 0) {
            return 0.0f;
        }
        for (int j = i; j < 3; j++) {
            numbers[j] = 0;
        }
        return (float) (numbers[0] * 100 + numbers[1] * 10 + numbers[2]);
    }