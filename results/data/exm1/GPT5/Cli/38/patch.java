private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (!token.startsWith("-") || token.length() == 1 || token.startsWith("--"))
        {
            return false;
        }

        // remove leading "-" and "=value"
        String rest = token.substring(1);
        int pos = rest.indexOf("=");
        String optName = pos == -1 ? rest : rest.substring(0, pos);

        // exact short option match (supports multi-char short names like "t1" or "last")
        if (options.hasShortOption(optName))
        {
            return true;
        }

        // check for several concatenated single-char short options like -ab
        if (optName.length() > 1)
        {
            boolean allCharsAreOptions = true;
            for (int i = 0; i < optName.length(); i++)
            {
                if (!options.hasShortOption(String.valueOf(optName.charAt(i))))
                {
                    allCharsAreOptions = false;
                    break;
                }
            }
            if (allCharsAreOptions)
            {
                return true;
            }
        }

        // handle attached value for single short option like -Dparam or -Dparam=value
        return options.hasShortOption(String.valueOf(optName.charAt(0)));
    }