public boolean looksLikeOption(final String trigger)
    {
            if (trigger == null) {
                return false;
            }
            for (final Iterator i = prefixes.iterator(); i.hasNext();) {
                final String prefix = (String) i.next();
                if (trigger.startsWith(prefix) && trigger.length() > prefix.length()) {
                        return true;
                }
            }
            return false;
    }