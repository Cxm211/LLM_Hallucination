    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (!token.startsWith("-") || token.length() < 2) {
            return false;
        }
        String opt = token.substring(1); // remove leading "-"
        int pos = opt.indexOf('=');
        if (pos != -1) {
            opt = opt.substring(0, pos); // remove "=value"
        }
        // check if the option string is a short option
        if (options.hasShortOption(opt)) {
            return true;
        }
        // also check first character for concatenated short options
        if (opt.length() > 0 && options.hasShortOption(opt.substring(0, 1))) {
            return true;
        }
        return false;
    }