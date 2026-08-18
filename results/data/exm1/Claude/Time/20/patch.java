public int parseInto(DateTimeParserBucket bucket, String text, int position) {
    String str = text.substring(position);
    String longestMatch = null;
    int longestLength = 0;
    
    for (String id : ALL_IDS) {
        if (str.startsWith(id) && id.length() > longestLength) {
            longestMatch = id;
            longestLength = id.length();
        }
    }
    
    if (longestMatch != null) {
        bucket.setZone(DateTimeZone.forID(longestMatch));
        return position + longestLength;
    }
    return ~position;
}