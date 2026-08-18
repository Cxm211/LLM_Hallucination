public boolean looksLikeOption(final String trigger)
    {
            if (trigger == null) {
                return false;
            }
            // "--" is the end-of-options marker and should not be treated as an option
            if ("--".equals(trigger)) {
                return false;
            }
            for (final Iterator i = prefixes.iterator(); i.hasNext();) {
                final String prefix = (String) i.next();

                if (trigger.startsWith(prefix))
                {
                        // require something after the prefix
                        if (trigger.length() == prefix.length()) {
                            continue;
                        }
                        // treat negative numbers (e.g. -42) as values, not options
                        if ("-".equals(prefix)) {
                            char next = trigger.charAt(prefix.length());
                            if (Character.isDigit(next)) {
                                return false;
                            }
                        }
                        return true;
                }
            }
            return false;
    }