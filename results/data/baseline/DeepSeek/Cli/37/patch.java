private boolean isShortOption(String token)
{
    // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
    // remove leading "-" and "=value"
    if (!token.startsWith("-") || token.length() < 2) return false;
    String opt = token.substring(1);
    int pos = opt.indexOf('=');
    if (pos != -1) {
        opt = opt.substring(0, pos);
    }
    return options.hasShortOption(opt);
}