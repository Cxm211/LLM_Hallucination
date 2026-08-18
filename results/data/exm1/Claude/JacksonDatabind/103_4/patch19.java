public boolean includeFilterSuppressNulls(Object filter) throws JsonMappingException
    {
        if (filter == null) {
            return true;
        }
        try {
            return filter.equals(null);
        } catch (Throwable t) {
            String msg = t.getMessage();
            String errMsg = String.format(
"Problem determining whether filter of type '%s' should filter out `null` values: (%s) %s",
filter.getClass().getName(), t.getClass().getName(), (msg == null) ? "N/A" : msg);
            reportBadDefinition(filter.getClass(), errMsg, t);
            return false;
        }
    }