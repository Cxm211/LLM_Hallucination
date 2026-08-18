    public boolean looksLikeOption(final String trigger)
    {
            // this is a reentrant call

            for (final Iterator i = prefixes.iterator(); i.hasNext();)
            {
                final String prefix = (String) i.next();

                if (trigger.startsWith(prefix))
                {
                        // Check if the trigger is a number (including negative numbers)
                        // to avoid treating negative numbers as options.
                        try {
                            Double.parseDouble(trigger);
                            return false;
                        } catch (NumberFormatException e) {
                            // Not a number, so treat as option.
                        }
                        return true;
                }
            }
            return false;
    }