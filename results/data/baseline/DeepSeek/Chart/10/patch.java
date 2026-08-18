    public String generateToolTipFragment(String toolTipText) {
        String escaped = toolTipText.replace("&", "&amp;").replace("\"", "&quot;");
        return " title=\"" + escaped
            + "\" alt=\"\"";
    }