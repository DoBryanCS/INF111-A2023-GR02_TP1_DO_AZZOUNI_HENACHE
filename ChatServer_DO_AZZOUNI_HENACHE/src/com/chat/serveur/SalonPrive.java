package com.chat.serveur;

import com.echecs.PartieEchecs;

public class SalonPrive {
    private String aliasHote, aliasInvite;
    private PartieEchecs partieEchecs;

    public SalonPrive(String aliasHote, String aliasInvite) {
        this.aliasHote = aliasHote;
        this.aliasInvite = aliasInvite;
    }

    public PartieEchecs getPartieEchecs() {
        return partieEchecs;
    }

    public String getAliasHote() {
        return aliasHote;
    }

    public String getAliasInvite() {
        return aliasInvite;
    }

    public void setPartieEchecs(PartieEchecs partieEchecs) {
        this.partieEchecs = partieEchecs;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) //les 2 objets sont en fait le m�me
            return true;
        if (obj == null)
            return false;
        if (obj instanceof SalonPrive) {
            SalonPrive salonPrive = (SalonPrive) obj;
            return (this.aliasHote.equals(salonPrive.aliasHote) && aliasInvite.equals(salonPrive.aliasInvite)) ||
                    (this.aliasHote.equals(salonPrive.aliasInvite) && aliasInvite.equals(salonPrive.aliasHote));
        }
        else
            return false;
    }
}
