public static FileInputStream openExistingFile(final String str)
{
    try
    {
        File file = new File(str);
        if (file.exists() && file.canRead())
        {
            return new FileInputStream(file);
        }
        return null;
    }
    catch (Exception e)
    {
        return null;
    }
}