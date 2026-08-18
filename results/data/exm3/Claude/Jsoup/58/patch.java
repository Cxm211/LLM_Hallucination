public static boolean isValid(String bodyHtml, Whitelist whitelist) {
    Document dirtyDocument = parseBodyFragment(bodyHtml, "");
    return new Cleaner(whitelist).isValid(dirtyDocument);
}