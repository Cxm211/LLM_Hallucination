// ===== FIXED org.apache.commons.cli.TypeHandler :: createValue(String, Class) [lines 63-105] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Cli/Cli-40-fixed/src/main/java/org/apache/commons/cli/TypeHandler.java =====
    public static <T> T createValue(final String str, final Class<T> clazz) throws ParseException
    {
        if (PatternOptionBuilder.STRING_VALUE == clazz)
        {
            return (T) str;
        }
        else if (PatternOptionBuilder.OBJECT_VALUE == clazz)
        {
            return (T) createObject(str);
        }
        else if (PatternOptionBuilder.NUMBER_VALUE == clazz)
        {
            return (T) createNumber(str);
        }
        else if (PatternOptionBuilder.DATE_VALUE == clazz)
        {
            return (T) createDate(str);
        }
        else if (PatternOptionBuilder.CLASS_VALUE == clazz)
        {
            return (T) createClass(str);
        }
        else if (PatternOptionBuilder.FILE_VALUE == clazz)
        {
            return (T) createFile(str);
        }
        else if (PatternOptionBuilder.EXISTING_FILE_VALUE == clazz)
        {
            return (T) openFile(str);
        }
        else if (PatternOptionBuilder.FILES_VALUE == clazz)
        {
            return (T) createFiles(str);
        }
        else if (PatternOptionBuilder.URL_VALUE == clazz)
        {
            return (T) createURL(str);
        }
        else
        {
            throw new ParseException("Unable to handle the class: " + clazz);
        }
    }
