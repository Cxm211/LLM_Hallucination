public void addOption(Option option) {
    options.add(option);
    nameToOption.put(option.getPreferredName(), option);

    for (Iterator i = option.getTriggers().iterator(); i.hasNext();) {
        nameToOption.put(i.next(), option);
    }

    // ensure that all parent options are also added
    if (option instanceof Group) {
        Group group = (Group) option;
        for (Iterator i = group.getOptions().iterator(); i.hasNext();) {
            addOption((Option) i.next());
        }
    }
}