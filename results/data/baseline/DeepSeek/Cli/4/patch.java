private void checkRequiredOptions() throws MissingOptionException {
    if (requiredOptions.size() > 0) {
        Iterator<Option> iter = requiredOptions.iterator();
        StringBuffer buff = new StringBuffer("Missing required option(s): ");
        while (iter.hasNext()) {
            buff.append(iter.next().getKey());
            if (iter.hasNext()) {
                buff.append(", ");
            }
        }
        throw new MissingOptionException(buff.toString());
    }
}