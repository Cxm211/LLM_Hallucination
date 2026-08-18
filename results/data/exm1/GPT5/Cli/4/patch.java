    private void checkRequiredOptions()
        throws MissingOptionException
    {
        // if there are required options that have not been
        // processsed
        if (requiredOptions.size() > 0)
        {
            Iterator iter = requiredOptions.iterator();
            StringBuffer buff = new StringBuffer();


            // loop through the required options
            while (iter.hasNext())
            {
                buff.append(iter.next());
            }

            if (requiredOptions.size() == 1) {
                throw new MissingOptionException("Missing required option: " + buff.toString());
            } else {
                throw new MissingOptionException("Missing required options: " + buff.toString());
            }
        }
    }