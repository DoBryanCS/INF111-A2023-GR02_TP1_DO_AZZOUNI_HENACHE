package com.chat.serveur;

public class SalonPrive {
    private String aliasHote, aliasInvite;

    public SalonPrive(String aliasHote, String aliasInvite) {
        this.aliasHote = aliasHote;
        this.aliasInvite = aliasInvite;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) //les 2 objets sont en fait le même
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
