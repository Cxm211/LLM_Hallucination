public boolean processStartTag(String name, Attributes attrs) {
        start.reset();
        start.nameAttr(name != null ? name.toLowerCase(java.util.Locale.ENGLISH) : name, attrs);
        return process(start);
    }