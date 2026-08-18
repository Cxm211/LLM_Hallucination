protected boolean processStartTag(String name) {
        return process(start.reset().name(name != null ? name.toLowerCase(java.util.Locale.ENGLISH) : name));
    }