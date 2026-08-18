private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2) and GNU-style long with single dash (-long, -long=val)
        if (token == null || !token.startsWith("-") || token.length() < 2 || token.startsWith("--")) {
            return false;
        }

        String remainder = token.substring(1);
        int eq = remainder.indexOf('=');
        String opt = (eq >= 0) ? remainder.substring(0, eq) : remainder;

        // Prefer exact matches first
        if (options.hasShortOption(opt)) {
            return true;
        }
        // Support single-dash long options (e.g., -long)
        if (options.hasLongOption(opt)) {
            return true;
        }
        // Fallback: traditional single-character short option at the start of the token
        return options.hasShortOption(remainder.substring(0, 1));

        // remove leading "-" and "=value"
    }