    private void checkRequiredOptions()
        throws MissingOptionException
    {
        // if there are required options that have not been
        // processsed
        if (requiredOptions.size() > 0)
        {
            throw new MissingOptionException(requiredOptions);
        }
    }