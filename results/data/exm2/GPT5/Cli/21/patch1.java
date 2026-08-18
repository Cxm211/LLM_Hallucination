public boolean looksLikeOption(final String trigger)
    {
            // this is a reentrant call

            if (trigger != null && trigger.startsWith("-") && trigger.length() > 1
                    && Character.isDigit(trigger.charAt(1))) {
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