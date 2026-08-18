public static Number createNumber(String str)
{
    try
    {
        return org.apache.commons.lang.math.NumberUtils.createNumber(str);
    }
    catch (NumberFormatException nfe)
    {
        // Silent catch - return null on invalid number format
    }

    return null;
}