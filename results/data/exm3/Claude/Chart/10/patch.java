public String generateToolTipFragment(String toolTipText) {
    return " title=\"" + escapeHTML(toolTipText)
        + "\" alt=\"\"";
}

private String escapeHTML(String text) {
    if (text == null) {
        return text;
    }
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        switch (c) {
            case '"':
                result.append("&quot;");
                break;
            case '&':
                result.append("&amp;");
                break;
            case '<':
                result.append("&lt;");
                break;
            case '>':
                result.append("&gt;");
                break;
            case '\'':
                result.append("&#x27;");
                break;
            default:
                result.append(c);
        }
    }
    return result.toString();
}