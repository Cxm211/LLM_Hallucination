private boolean isShortOption(String token)
{
    // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
    if (!token.startsWith("-") || token.length() < 2) {
        return false;
    }
    int eq = token.indexOf('=');
    String opt = eq == -1 ? token.substring(1) : token.substring(1, eq);
    return options.hasShortOption(opt);
}