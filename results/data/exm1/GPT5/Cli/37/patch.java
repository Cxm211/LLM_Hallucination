    private boolean isShortOption(String token)
    {
        // short options (-S, -SV, -S=V, -SV1=V2, -S1S2)
        if (token == null || token.length() < 2 || !token.startsWith("-")) {
            return false;
        }
        // not a short option if it starts with "--"
        if (token.startsWith("--")) {
            return false;
        }

        // remove leading "-" and "=value"
        String t = token.substring(1);
        int eq = t.indexOf('=');
        if (eq != -1) {
            t = t.substring(0, eq);
        }
        if (t.isEmpty()) {
            return false;
        }

        // support multi-character short options
        if (options.hasShortOption(t)) {
            return true;
        }
        // fallback to single-character short option (e.g., -Dparam=value)
        return options.hasShortOption(t.substring(0, 1));

        // remove leading "-" and "=value"
    }