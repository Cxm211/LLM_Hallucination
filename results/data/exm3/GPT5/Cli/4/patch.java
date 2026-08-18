private void checkRequiredOptions()
        throws MissingOptionException
    {
        if (requiredOptions.size() > 0)
        {
            StringBuffer buff = new StringBuffer();
            if (requiredOptions.size() == 1)
            {
                buff.append("Missing required option: ");
            }
            else
            {
                buff.append("Missing required options: ");
            }

            Iterator iter = requiredOptions.iterator();
            while (iter.hasNext())
            {
                buff.append(iter.next());
            }

            throw new MissingOptionException(buff.toString());
        }
    }