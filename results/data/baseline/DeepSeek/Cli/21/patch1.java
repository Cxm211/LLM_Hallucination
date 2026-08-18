    public boolean looksLikeOption(final String trigger)
    {
            // this is a reentrant call
            if (prefixes == null || trigger == null) {
                return false;
            }
            for (final Iterator i = prefixes.iterator(); i.hasNext();)
            {
                final String prefix = (String) i.next();

                if (trigger.startsWith(prefix))
                {
                        return true;
                }
            }
            return false;
    }