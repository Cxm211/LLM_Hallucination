public List<String> getMatchingOptions(String opt) {
    opt = Util.stripLeadingHyphens(opt);
    
    // Check for perfect match first
    if (longOpts.containsKey(opt)) {
        List<String> matchingOpts = new ArrayList<String>();
        matchingOpts.add(opt);
        return matchingOpts;
    }
    
    List<String> matchingOpts = new ArrayList<String>();
    for (String longOpt : longOpts.keySet()) {
        if (longOpt.startsWith(opt)) {
            matchingOpts.add(longOpt);
        }
    }
    return matchingOpts;
}