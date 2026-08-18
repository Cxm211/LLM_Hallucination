public static Number createNumber(String str)
{
    try
    {
        return org.apache.commons.lang.math.NumberUtils.createNumber(str);
    }
    catch (NumberFormatException nfe)
    {
        // Do not print to stderr in production code
        // System.err.println(nfe.getMessage());
        throw nfe;
    }
}