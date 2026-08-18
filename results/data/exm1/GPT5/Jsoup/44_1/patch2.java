protected boolean processEndTag(String name) {
        return process(end.reset().name(name != null ? name.toLowerCase(java.util.Locale.ENGLISH) : name));
    }