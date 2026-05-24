package model;

public enum LocationV {
    GOLAN_HEIGHTS("Golan Heights"),
    KINNERET("Kinneret"),
    SAFED("Safed"),
    AKKO("Akko"),
    JEZREEL("Jezreel"),
    HAIFA("Haifa"),
    HADERA("Hadera"),
    SHARON("Sharon"),
    TEL_AVIV_JAFFA("Tel Aviv-Jaffa"),
    PETAH_TIKVA("Petah Tikva"),
    RAMLA("Ramla"),
    REHOVOT("Rehovot"),
    JERUSALEM("Jerusalem"),
    ASHKELON("Ashkelon"),
    BEERSHEBA("Beersheba"),
    ManPowerLocation("ManPowerLocation");

    private final String displayName;

    LocationV(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
