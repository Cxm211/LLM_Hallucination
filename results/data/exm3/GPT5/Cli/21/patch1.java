public boolean looksLikeOption(final String trigger)
    {
            if (trigger == null) {
                return false;
            }

            for (final Iterator i = prefixes.iterator(); i.hasNext();)
            {
                final String prefix = (String) i.next();

                if (trigger.startsWith(prefix))
                {
                        // If the token is exactly the prefix (e.g. "-" or "--"), treat as option-like
                        if (trigger.length() == prefix.length()) {
                            return true;
                        }
                        // If the first character after the prefix is a digit, this is likely a negative number
                        // and should NOT be treated as an option name.
                        final char next = trigger.charAt(prefix.length());
                        if (Character.isDigit(next)) {
                            return false;
                        }
                        return true;
                }
            }
            return false;
    }