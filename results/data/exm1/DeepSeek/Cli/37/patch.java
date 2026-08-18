    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (!token.startsWith("-") || token.length() < 2) {
            return false;
        }
        // remove leading "-" and "=value"
        String opt = token.substring(1);
        int pos = opt.indexOf('=');
        if (pos != -1) {
            opt = opt.substring(0, pos);
        }
        // first try the entire string as a short option
        if (options.hasShortOption(opt)) {
            return true;
        }
        // if not, try just the first character
        return options.hasShortOption(token.substring(1, 2));
    }