private static final Map<String, String> cZoneIdConversion = new HashMap<String, String>();
static {
    cZoneIdConversion.put("GMT", "UTC");
    cZoneIdConversion.put("MIT", "Pacific/Apia");
    cZoneIdConversion.put("HST", "Pacific/Honolulu");
    cZoneIdConversion.put("AST", "America/Anchorage");
    cZoneIdConversion.put("PST", "America/Los_Angeles");
    cZoneIdConversion.put("MST", "America/Denver");
    cZoneIdConversion.put("PNT", "America/Phoenix");
    cZoneIdConversion.put("CST", "America/Chicago");
    cZoneIdConversion.put("EST", "America/New_York");
    cZoneIdConversion.put("IET", "America/Indianapolis");
    cZoneIdConversion.put("PRT", "America/Puerto_Rico");
    cZoneIdConversion.put("CNT", "America/St_Johns");
    cZoneIdConversion.put("AGT", "America/Buenos_Aires");
    cZoneIdConversion.put("BET", "America/Sao_Paulo");
    cZoneIdConversion.put("WET", "Europe/London");
    cZoneIdConversion.put("ECT", "Europe/Paris");
    cZoneIdConversion.put("ART", "Africa/Cairo");
    cZoneIdConversion.put("CAT", "Africa/Harare");
    cZoneIdConversion.put("EET", "Europe/Bucharest");
    cZoneIdConversion.put("EAT", "Africa/Addis_Ababa");
    cZoneIdConversion.put("MET", "Asia/Tehran");
    cZoneIdConversion.put("NET", "Asia/Yerevan");
    cZoneIdConversion.put("PLT", "Asia/Karachi");
    cZoneIdConversion.put("IST", "Asia/Calcutta");
    cZoneIdConversion.put("BST", "Asia/Dhaka");
    cZoneIdConversion.put("VST", "Asia/Saigon");
    cZoneIdConversion.put("CTT", "Asia/Shanghai");
    cZoneIdConversion.put("JST", "Asia/Tokyo");
    cZoneIdConversion.put("ACT", "Australia/Darwin");
    cZoneIdConversion.put("AET", "Australia/Sydney");
    cZoneIdConversion.put("SST", "Pacific/Guadalcanal");
    cZoneIdConversion.put("NST", "Pacific/Auckland");
}
private static synchronized String getConvertedId(String id) {
    return cZoneIdConversion.get(id);
}