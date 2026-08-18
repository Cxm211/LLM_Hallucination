public String generateToolTipFragment(String toolTipText) {
    return " title=\"" + HTMLToolTipTagFragmentGenerator.htmlEscape(toolTipText)
        + "\" alt=\"\"";
}