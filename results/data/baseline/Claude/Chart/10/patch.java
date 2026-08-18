public String generateToolTipFragment(String toolTipText) {
    return " title=\"" + escapeHtml(toolTipText)
        + "\" alt=\"\"";
}

private String escapeHtml(String text) {
    if (text == null) {
        return "";
    }
    return text.replace("&", "&amp;")
               .replace("<", "&lt;")
               .replace(">", "&gt;")
               .replace("\"", "&quot;")
               .replace("'", "&#x27;");
}