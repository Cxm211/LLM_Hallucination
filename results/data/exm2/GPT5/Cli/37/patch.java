private boolean isShortOption(String token)
{
    if (token == null) return false;
    if (!token.startsWith("-") || token.length() < 2) return false;
    String t = token.substring(1);
    int eq = t.indexOf('=');
    String name = (eq >= 0) ? t.substring(0, eq) : t;
    if (options.hasShortOption(name)) return true;
    return options.hasShortOption(t.substring(0, 1));
}