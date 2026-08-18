public String generateToolTipFragment(String toolTipText) {
    String text = toolTipText;
    if (text != null) {
        text = text.replace("\"", "&quot;");
    }
    return " title=\"" + text
        + "\" alt=\"\"";
}