public WriteableCommandLineImpl(final Option rootOption,
                                    final List arguments) {
        this.prefixes = rootOption.getPrefixes();
        this.normalised = new ArrayList();
        if (arguments != null) {
            this.normalised.addAll(arguments);
        }
    }