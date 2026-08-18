public WriteableCommandLineImpl(final Option rootOption,
                                final List arguments) {
    this.prefixes = (rootOption != null) ? rootOption.getPrefixes() : new HashSet();
    this.normalised = arguments;
}