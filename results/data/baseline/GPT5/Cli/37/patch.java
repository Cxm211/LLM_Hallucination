private boolean isShortOption(String token)
    {
        if (token == null || token.length() < 2) {
            return false;
        }
        if (!token.startsWith("-") || token.startsWith("--")) {
            return false;
        }
        return options.hasShortOption(token.substring(1, 2));
    }