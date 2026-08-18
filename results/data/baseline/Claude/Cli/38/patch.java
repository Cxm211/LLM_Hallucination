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
    
    // check if it's a single short option
    if (options.hasShortOption(optName))
    {
        return true;
    }
    
    // check for several concatenated short options
    if (pos == -1 && optName.length() > 0)
    {
        return options.hasShortOption(String.valueOf(optName.charAt(0)));
    }
    
    return false;
}