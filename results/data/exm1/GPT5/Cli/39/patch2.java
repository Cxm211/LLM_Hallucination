public static FileInputStream createExistingFile(final String str) throws ParseException
    {
        final File file = new File(str);
        if (!file.exists() || !file.isFile() || !file.canRead())
        {
            return null;
        }
        try
        {
            return new FileInputStream(file);
        }
        catch (final FileNotFoundException e)
        {
            return null;
        }
    }