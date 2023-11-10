package com.chat.serveur;

import com.chat.commun.evenement.Evenement;
import com.chat.commun.evenement.EvenementUtil;
import com.chat.commun.evenement.GestionnaireEvenement;
import com.chat.commun.net.Connexion;

/**
 * Cette classe repr�sente un gestionnaire d'�v�nement d'un serveur. Lorsqu'un serveur re�oit un texte d'un client,
 * il cr�e un �v�nement � partir du texte re�u et alerte ce gestionnaire qui r�agit en g�rant l'�v�nement.
 *
 * @author Abdelmoum�ne Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class GestionnaireEvenementServeur implements GestionnaireEvenement {
    private Serveur serveur;

    /**
     * Construit un gestionnaire d'�v�nements pour un serveur.
     *
     * @param serveur Serveur Le serveur pour lequel ce gestionnaire g�re des �v�nements
     */
    public GestionnaireEvenementServeur(Serveur serveur) {
        this.serveur = serveur;
    }

    /**
     * M�thode de gestion d'�v�nements. Cette m�thode contiendra le code qui g�re les r�ponses obtenues d'un client.
     *
     * @param evenement L'�v�nement � g�rer.
     */
    @Override
    public void traiter(Evenement evenement) {
        Object source = evenement.getSource();
        Connexion cnx;
        String msg, typeEvenement, aliasExpediteur, aliasInvite, listInvitations;
        boolean salonPriveExiste, utilisateurExiste, invitationSoi;
        Invitation invitation;
        ServeurChat serveur = (ServeurChat) this.serveur;

        if (source instanceof Connexion) {
            cnx = (Connexion) source;
            System.out.println("SERVEUR-Recu : " + evenement.getType() + " " + evenement.getArgument());
            typeEvenement = evenement.getType();
            switch (typeEvenement) {
                case "EXIT": //Ferme la connexion avec le client qui a envoy� "EXIT":
                    cnx.envoyer("END");
                    serveur.enlever(cnx);
                    cnx.close();
                    break;
                case "LIST": //Envoie la liste des alias des personnes connect�es :
                    cnx.envoyer("LIST " + serveur.list());
                    break;

                //Ajoutez ici d�autres case pour g�rer d�autres commandes.

                case "MSG": //Envoie un message � tous les utilisateurs connect�s sauf l'exp�diteur :
                    aliasExpediteur = cnx.getAlias();
                    msg = evenement.getArgument();
                    serveur.envoyerATousSauf(msg, aliasExpediteur);
                    break;

                case "HIST": //Affiche l'historique des messages :
                    cnx.envoyer("HIST " + serveur.historique());
                    break;

                case "JOIN": //Invite un utilisateur � chatter en priv� ou accepte l�invitation qui lui a �t�
                    //pr�alablement envoy�e par un utilisateur :
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();


                    salonPriveExiste = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);
                    utilisateurExiste = serveur.verifierExistenceUtilisateur(aliasInvite);
                    invitationSoi = aliasExpediteur.equals(aliasInvite);

                    if (utilisateurExiste && !invitationSoi && !salonPriveExiste) {
                        invitation = serveur.verifierExistenceInvitation(aliasExpediteur, aliasInvite);

                        if (invitation != null) {
                            serveur.traiterJoinInvitationExistente(cnx, aliasExpediteur, aliasInvite, invitation);
                        } else {
                            serveur.invitations.add(new Invitation(aliasExpediteur, aliasInvite));
                            cnx.envoyer("Succ�s de l'invitation!");
                            serveur.envoyerMessagePrive(aliasInvite).envoyer("JOIN " + aliasExpediteur);
                        }
                    }

                    if (salonPriveExiste)
                        cnx.envoyer("Le salon priv� avec " + aliasInvite + " existe d�ja!");

                    if (!utilisateurExiste)
                        cnx.envoyer("L'utilisateur " + aliasInvite + " n'existe pas!");

                    if (invitationSoi)
                        cnx.envoyer("Vous ne pouvez pas vous inviter vous-m�me!");

                    break;

                case "DECLINE": //Refuse une invitation � chatter en priv� d'un utilisateur ou annule une invitation
                    //qu'il a pr�alablement envoy�e � un utilisateur :
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();

                    salonPriveExiste = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);
                    utilisateurExiste = serveur.verifierExistenceUtilisateur(aliasInvite);
                    invitationSoi = aliasExpediteur.equals(aliasInvite);

                    if (utilisateurExiste && !invitationSoi && !salonPriveExiste) {
                        invitation = serveur.verifierExistenceInvitation(aliasInvite, aliasExpediteur);

                        if (invitation != null) {
                            serveur.traiterDeclineInvitation(cnx, aliasExpediteur, aliasInvite);
                        } else {
                            cnx.envoyer("Aucune invitation � refuser ou � annuler en lien avec l'utilisateur "
                                    + aliasInvite + "!");
                        }
                    }

                    if (salonPriveExiste)
                        cnx.envoyer("Le salon priv� avec " + aliasInvite + " existe d�ja!");

                    if (!utilisateurExiste)
                        cnx.envoyer("L'utilisateur " + aliasInvite + " n'existe pas!");

                    if (invitationSoi)
                        cnx.envoyer("Vous ne pouvez pas vous refuser ou annuler une invitation � vous-m�me!");

                    break;

                case "INV": //Obtenir la liste de tous les alias des personnes qui lui ont envoy� des invitations :
                    aliasExpediteur = cnx.getAlias();

                    listInvitations = serveur.listInvitations(aliasExpediteur);
                    cnx.envoyer("INV " + listInvitations);

                    break;

                case "PRV": //Envoyer un message � un utilisateur dans un salon priv� :
                    aliasExpediteur = cnx.getAlias();
                    String aliasMessage = evenement.getArgument();
                    String[] t;

                    t = EvenementUtil.extraireInfosEvenement(aliasMessage);
                    aliasInvite = t[0];
                    msg = aliasExpediteur + ">>" + t[1];

                    salonPriveExiste = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);

                    if (salonPriveExiste)
                        serveur.envoyerMessagePrive(aliasInvite).envoyer(msg);
                    else
                        cnx.envoyer("Le salon priv� avec " + aliasInvite + " n'existe pas!");

                    break;

                case "QUIT": //Quitter le salon priv� avec un utilisateur :
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();

                    salonPriveExiste = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);

                    if (salonPriveExiste) {
                        serveur.salonsPrives.remove(new SalonPrive(aliasExpediteur, aliasInvite));
                        cnx.envoyer("Vous avez quitt� le salon priv� avec succ�s!");
                        serveur.envoyerMessagePrive(aliasInvite).envoyer("QUIT " + aliasExpediteur);
                    } else
                        cnx.envoyer("Le salon priv� avec " + aliasInvite + " n'existe pas!");

                    break;

                default: //Renvoyer le texte recu convertit en majuscules :
                    msg = (evenement.getType() + " " + evenement.getArgument()).toUpperCase();
                    cnx.envoyer(msg);
            }
        }
    }
}