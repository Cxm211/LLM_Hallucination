private boolean isShortOption(String token)
{
    // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
    if (!token.startsWith("-") || token.length() == 1)
    {
        return false;
    }

    // remove leading "-" and "=value"
    int pos = token.indexOf("=");
    String optName = pos == -1 ? token.substring(1) : token.substring(1, pos);
    
    // check if it's a known short option (single character)
    if (optName.length() == 1 && options.hasShortOption(optName)) {
        return true;
    }
    
    // check for several concatenated short options
    if (pos == -1) {
        for (int i = 0; i < optName.length(); i++) {
            if (!options.hasShortOption(String.valueOf(optName.charAt(i)))) {
                return false;
            }
        }
        return optName.length() > 0;
    }
    
    return false;
}