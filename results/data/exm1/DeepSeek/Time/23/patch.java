private static synchronized String getConvertedId(String id) {
    Map<String, String> map = cZoneIdConversion;
    if (map == null) {
        map = new HashMap<String, String>();
        map.put("GMT", "UTC");
        map.put("MIT", "Pacific/Apia");
        map.put("HST", "Pacific/Honolulu");
        map.put("AST", "America/Anchorage");
        map.put("PST", "America/Los_Angeles");
        map.put("MST", "America/Denver");
        map.put("PNT", "America/Phoenix");
        map.put("CST", "America/Chicago");
        map.put("EST", "America/New_York");
        map.put("IET", "America/Indiana/Indianapolis");
        map.put("PRT", "America/Puerto_Rico");
        map.put("CNT", "America/St_Johns");
        map.put("AGT", "America/Argentina/Buenos_Aires");
        map.put("BET", "America/Sao_Paulo");
        map.put("WET", "WET");
        map.put("ECT", "CET");
        map.put("ART", "Africa/Cairo");
        map.put("CAT", "Africa/Harare");
        map.put("EET", "EET");
        map.put("EAT", "Africa/Addis_Ababa");
        map.put("MET", "CET");
        map.put("NET", "Asia/Yerevan");
        map.put("PLT", "Asia/Karachi");
        map.put("IST", "Asia/Kolkata");
        map.put("BST", "Asia/Dhaka");
        map.put("VST", "Asia/Ho_Chi_Minh");
        map.put("CTT", "Asia/Shanghai");
        map.put("JST", "Asia/Tokyo");
        map.put("ACT", "Australia/Darwin");
        map.put("AET", "Australia/Sydney");
        map.put("SST", "Pacific/Guadalcanal");
        map.put("NST", "Pacific/Auckland");
        map.put("CET", "CET");
        cZoneIdConversion = map;
    }
    return map.get(id);
}