private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (token == null || !token.startsWith("-") || token.length() == 1)
        {
            return false;
        }

        // exclude long options starting with "--"
        if (token.startsWith("--"))
        {
            return false;
        }

        // remove leading "-" and "=value"
        int pos = token.indexOf("=");
        String optName = pos == -1 ? token.substring(1) : token.substring(1, pos);

        if (optName.length() == 0)
        {
            return false;
        }

        // exact short option match (supports multi-character short options like -t1)
        if (options.hasShortOption(optName))
        {
            return true;
        }

        // check for several concatenated single-character short options like -ab or -abc
        for (int i = 0; i < optName.length(); i++)
        {
            String chOpt = String.valueOf(optName.charAt(i));
            if (!options.hasShortOption(chOpt))
            {
                return false;
            }
        }
        return true;
        // check for several concatenated short options
    }