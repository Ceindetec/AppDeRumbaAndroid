package org.ceindetec.d3rumb4;

public final class GlobalVars {

    private int sede;
    private int estadoRegistroUsuario;
    private String nickname;
    private String urlBase = "http://192.168.0.245/derumba/";

    private static GlobalVars globalVarsInstance;

    protected GlobalVars() {
    }

    public static synchronized GlobalVars getGlobalVarsInstance() {
        if (null == globalVarsInstance) {
            globalVarsInstance = new GlobalVars();
        }
        return globalVarsInstance;
    }
    synchronized public int getEstadoRegistroUsuario() {
        return estadoRegistroUsuario;
    }

    synchronized public void setEstadoRegistroUsuario(int estadoRegistroUsuario) { this.estadoRegistroUsuario = estadoRegistroUsuario; }

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

    synchronized public String getUrlBase() {
        return urlBase;
    }

    synchronized public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }
}
