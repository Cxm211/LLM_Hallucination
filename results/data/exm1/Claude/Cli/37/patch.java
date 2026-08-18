private boolean isShortOption(String token)
{
    // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
    if (!token.startsWith("-") || token.length() < 2) {
        return false;
    }
    
    // Check if the first character after '-' is a valid short option
    int firstCharPos = 1;
    if (options.hasShortOption(token.substring(firstCharPos, firstCharPos + 1))) {
        return true;
    }
    
    return false;
}