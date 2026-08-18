public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    List<String> sortedIds = new ArrayList<String>(ALL_IDS);
    Collections.sort(sortedIds, new Comparator<String>() {
        public int compare(String a, String b) {
            return b.length() - a.length();
        }
    });
    for (String id : sortedIds) {
        if (str.startsWith(id)) {
            bucket.setZone(DateTimeZone.forID(id));
            return position + id.length();
        }
    }
    return ~position;
}