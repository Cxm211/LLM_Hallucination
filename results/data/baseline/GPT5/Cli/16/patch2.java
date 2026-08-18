public GroupImpl(final List options,
                 final String name,
                 final String description,
                 final int minimum,
                 final int maximum) {
    super(0, false);

    this.name = name;
    this.description = description;
    this.minimum = minimum;
    this.maximum = maximum;

    final List nonAnonymous = new ArrayList();

    // anonymous Argument temporary storage
    final List newAnonymous = new ArrayList();

    // map (key=trigger & value=Option) temporary storage
    final SortedMap newOptionMap = new TreeMap(ReverseStringComparator.getInstance());

    // prefixes temporary storage
    final Set newPrefixes = new HashSet();

    // process the options without mutating the input list
    for (final Iterator i = options.iterator(); i.hasNext();) {
        final Option option = (Option) i.next();

        if (option instanceof Argument) {
            newAnonymous.add(option);
        } else {
            nonAnonymous.add(option);
            final Set triggers = option.getTriggers();

            for (Iterator j = triggers.iterator(); j.hasNext();) {
                newOptionMap.put(j.next(), option);
            }

            // store the prefixes
            newPrefixes.addAll(option.getPrefixes());
        }
    }

    // store an unmodifiable copy of non-anonymous options
    this.options = Collections.unmodifiableList(new ArrayList(nonAnonymous));

    this.anonymous = Collections.unmodifiableList(newAnonymous);
    this.optionMap = Collections.unmodifiableSortedMap(newOptionMap);
    this.prefixes = Collections.unmodifiableSet(newPrefixes);
}