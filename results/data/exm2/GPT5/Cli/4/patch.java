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

            String msg;
            if (requiredOptions.size() == 1) {
                msg = "Missing required option: " + buff.toString();
            } else {
                msg = "Missing required options: " + buff.toString();
            }

            throw new MissingOptionException(msg);
        }
    }