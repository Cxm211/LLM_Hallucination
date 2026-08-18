public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    String longestId = null;
    int longestLen = -1;
    for (String id : ALL_IDS) {
        if (str.startsWith(id) && id.length() > longestLen) {
            longestId = id;
            longestLen = id.length();
        }
    }
    if (longestId != null) {
        bucket.setZone(DateTimeZone.forID(longestId));
        return position + longestLen;
    }
    return ~position;
}