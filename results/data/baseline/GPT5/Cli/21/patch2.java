private boolean looksLikeOption(final WriteableCommandLine commandLine,
            final String trigger) {
            if (commandLine == null) {
                return false;
            }
            return commandLine.looksLikeOption(trigger);
    }