public static Number createNumber(String str)
{
    try
    {
        return org.apache.commons.lang.math.NumberUtils.createNumber(str);
    }
    catch (NumberFormatException nfe)
    {
    }

    return null;
}