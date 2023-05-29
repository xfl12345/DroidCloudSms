package cc.xfl12345.android.droidcloudsms.model;

public class AndroidPermissionNamePair {

    private String displayName;

    private String codeName;

    public String getCodeName() {
        return codeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public AndroidPermissionNamePair(String codeName, String displayName) {
        this.codeName = codeName;
        this.displayName = displayName;
    }

}
