package com.chat.serveur;

public class Invitation {
    private String aliasHote, aliasInvite;
    private boolean isAliasHote, isAliasInvite, isInvitationEchec;

    public Invitation(String aliasHote, String aliasInvite, boolean isInvitationEchec) {
        this.aliasHote = aliasHote;
        this.aliasInvite = aliasInvite;
        this.isInvitationEchec = isInvitationEchec;
    }

    public String getAliasHote() {
        return aliasHote;
    }

    public String getAliasInvite() {
        return aliasInvite;
    }

    public boolean getIsInvitationEchec() { return isInvitationEchec; }

    public boolean getIsAliasHote() {
        return isAliasHote;
    }

    public boolean getIsAliasInvite() {
        return isAliasInvite;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) //les 2 objets sont en fait le m�me
            return true;
        if (obj == null)
            return false;
        if (obj instanceof Invitation) {
            Invitation invitation = (Invitation) obj;
            if ((this.aliasHote.equals(invitation.aliasHote) && aliasInvite.equals(invitation.aliasInvite)) ||
                    (this.aliasHote.equals(invitation.aliasInvite) && aliasInvite.equals(invitation.aliasHote))) {
                isAliasHote = this.aliasHote.equals(invitation.aliasHote);
                isAliasInvite = !isAliasHote;

                return true;
            } else
                return false;
        } else
            return false;
    }
}
