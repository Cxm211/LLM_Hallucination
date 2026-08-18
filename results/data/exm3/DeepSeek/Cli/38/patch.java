    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (!token.startsWith("-") || token.length() == 1)
        {
            return false;
        }
        // long options start with "--"
        if (token.startsWith("--"))
        {
            return false;
        }

        // remove leading "-" and "=value"
        int pos = token.indexOf("=");
        String optName = pos == -1 ? token.substring(1) : token.substring(1, pos);
        if (options.hasShortOption(optName)) {
            return true;
        }
        if (optName.length() > 0 && options.hasShortOption(String.valueOf(optName.charAt(0)))) {
            return true;
        }
        return false;
    }