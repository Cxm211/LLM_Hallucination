private boolean isShortOption(String token)
{
    // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
    if (!token.startsWith("-") || token.length() < 2) {
        return false;
    }
    if (token.startsWith("--")) {
        return false;
    }
    return options.hasShortOption(token.substring(1, 2));

    // remove leading "-" and "=value"
}