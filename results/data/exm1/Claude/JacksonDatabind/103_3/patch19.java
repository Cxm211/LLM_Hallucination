public boolean includeFilterSuppressNulls(Object filter) throws JsonMappingException
    {
        if (filter == null) {
            return true;
        }
        try {
            return filter.equals(null);
        } catch (Throwable t) {
            String msg = String.format(
"Problem determining whether filter of type '%s' should filter out `null` values: (%s) %s",
filter.getClass().getName(), t.getClass().getName(), t.getMessage());
            reportBadDefinition(filter.getClass(), msg, t);
            return false;
        }
    }