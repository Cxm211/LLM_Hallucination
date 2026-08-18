    public boolean looksLikeOption(final String trigger)
    {
            // this is a reentrant call

            for (final Iterator i = prefixes.iterator(); i.hasNext();)
            {
                final String prefix = (String) i.next();

                if (trigger.startsWith(prefix))
                {
                        String suffix = trigger.substring(prefix.length());
                        try {
                            Double.parseDouble(suffix);
                            // suffix is a number, so not an option
                            return false;
                        } catch (NumberFormatException e) {
                            // not a number, so looks like an option
                            return true;
                        }
                }
            }
            return false;
    }