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
            if (iter.hasNext())
            {
                buff.append(", ");
            }
        }

        throw new MissingOptionException(buff.toString());
    }
}