public boolean looksLikeOption(final String trigger)
{
        // this is a reentrant call

        for (final Iterator i = prefixes.iterator(); i.hasNext();)
        {
            final String prefix = (String) i.next();

            if (trigger.startsWith(prefix))
            {
                    // Check if it's a negative number: starts with prefix followed by a digit
                    if (prefix.equals("-") && trigger.length() > 1) {
                        char nextChar = trigger.charAt(1);
                        if (Character.isDigit(nextChar)) {
                            return false;
                        }
                    }
                    return true;
            }
        }
        return false;
}