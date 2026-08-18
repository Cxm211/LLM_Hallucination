public boolean looksLikeOption(final String trigger)
{
        // this is a reentrant call

        for (final Iterator i = prefixes.iterator(); i.hasNext();)
        {
            final String prefix = (String) i.next();

            if (trigger.startsWith(prefix))
            {
                    // Check if this looks like a negative number
                    if (trigger.length() > prefix.length()) {
                        char nextChar = trigger.charAt(prefix.length());
                        if (Character.isDigit(nextChar)) {
                            return false;
                        }
                    }
                    return true;
            }
        }
        return false;
}