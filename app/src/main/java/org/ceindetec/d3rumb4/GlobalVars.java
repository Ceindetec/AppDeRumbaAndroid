package org.ceindetec.d3rumb4;

public final class GlobalVars {

    private int sede;
    private String nickname;

    private static GlobalVars globalVarsInstance;

    protected GlobalVars() {
    }

    public static synchronized GlobalVars getGlobalVarsInstance() {
        if (null == globalVarsInstance) {
            globalVarsInstance = new GlobalVars();
        }
        return globalVarsInstance;
    }

    synchronized public int getSede() {
        return sede;
    }

    synchronized public void setSede(int sede) {
        this.sede = sede;
    }

    synchronized public String getNickname() {
        return nickname;
    }

    synchronized public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
