public void combine(ExtendedProperties props) {
    List<String> keys = new ArrayList<String>();
    for (Iterator it = props.getKeys(); it.hasNext(); ) {
        keys.add((String) it.next());
    }
    for (String key : keys) {
        setProperty(key, props.get(key));
    }
}