public void setSelected(Option option) throws AlreadySelectedException {
    if (option == null) {
        selected = null;
        return;
    }
    String opt = option.getOpt();
    String longOpt = option.getLongOpt();
    if (selected == null || selected.equals(opt) || (longOpt != null && selected.equals(longOpt))) {
        selected = opt;
    } else {
        throw new AlreadySelectedException(this, option);
    }
}