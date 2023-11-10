package com.chat.serveur;

import com.chat.commun.evenement.Evenement;
import com.chat.commun.evenement.EvenementUtil;
import com.chat.commun.evenement.GestionnaireEvenement;
import com.chat.commun.net.Connexion;

/**
 * Cette classe représente un gestionnaire d'événement d'un serveur. Lorsqu'un serveur reçoit un texte d'un client,
 * il crée un événement à partir du texte reçu et alerte ce gestionnaire qui réagit en gérant l'événement.
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class GestionnaireEvenementServeur implements GestionnaireEvenement {
    private Serveur serveur;

    /**
     * Construit un gestionnaire d'événements pour un serveur.
     *
     * @param serveur Serveur Le serveur pour lequel ce gestionnaire gère des événements
     */
    public GestionnaireEvenementServeur(Serveur serveur) {
        this.serveur = serveur;
    }

    /**
     * Méthode de gestion d'événements. Cette méthode contiendra le code qui gère les réponses obtenues d'un client.
     *
     * @param evenement L'événement à gérer.
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
                case "EXIT": //Ferme la connexion avec le client qui a envoyé "EXIT":
                    cnx.envoyer("END");
                    serveur.enlever(cnx);
                    cnx.close();
                    break;
                case "LIST": //Envoie la liste des alias des personnes connectées :
                    cnx.envoyer("LIST " + serveur.list());
                    break;

                //Ajoutez ici d’autres case pour gérer d’autres commandes.

                case "MSG": //Envoie un message à tous les utilisateurs connectés sauf l'expéditeur :
                    aliasExpediteur = cnx.getAlias();
                    msg = evenement.getArgument();
                    serveur.envoyerATousSauf(msg, aliasExpediteur);
                    break;

                case "HIST": //Affiche l'historique des messages :
                    cnx.envoyer("HIST " + serveur.historique());
                    break;

                case "JOIN": //Invite un utilisateur à chatter en privé ou accepte l’invitation qui lui a été
                    //préalablement envoyée par un utilisateur :
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
                            cnx.envoyer("Succès de l'invitation!");
                            serveur.envoyerMessagePrive(aliasInvite).envoyer("JOIN " + aliasExpediteur);
                        }
                    }

                    if (salonPriveExiste)
                        cnx.envoyer("Le salon privé avec " + aliasInvite + " existe déja!");

                    if (!utilisateurExiste)
                        cnx.envoyer("L'utilisateur " + aliasInvite + " n'existe pas!");

                    if (invitationSoi)
                        cnx.envoyer("Vous ne pouvez pas vous inviter vous-même!");

                    break;

                case "DECLINE": //Refuse une invitation à chatter en privé d'un utilisateur ou annule une invitation
                    //qu'il a préalablement envoyée à un utilisateur :
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
                            cnx.envoyer("Aucune invitation à refuser ou à annuler en lien avec l'utilisateur "
                                    + aliasInvite + "!");
                        }
                    }

                    if (salonPriveExiste)
                        cnx.envoyer("Le salon privé avec " + aliasInvite + " existe déja!");

                    if (!utilisateurExiste)
                        cnx.envoyer("L'utilisateur " + aliasInvite + " n'existe pas!");

                    if (invitationSoi)
                        cnx.envoyer("Vous ne pouvez pas vous refuser ou annuler une invitation à vous-même!");

                    break;

                case "INV": //Obtenir la liste de tous les alias des personnes qui lui ont envoyé des invitations :
                    aliasExpediteur = cnx.getAlias();

                    listInvitations = serveur.listInvitations(aliasExpediteur);
                    cnx.envoyer("INV " + listInvitations);

                    break;

                case "PRV": //Envoyer un message à un utilisateur dans un salon privé :
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
                        cnx.envoyer("Le salon privé avec " + aliasInvite + " n'existe pas!");

                    break;

                case "QUIT": //Quitter le salon privé avec un utilisateur :
                    aliasExpediteur = cnx.getAlias();
                    aliasInvite = evenement.getArgument();

                    salonPriveExiste = serveur.verifierExistenceSalonPrive(aliasExpediteur, aliasInvite);

                    if (salonPriveExiste) {
                        serveur.salonsPrives.remove(new SalonPrive(aliasExpediteur, aliasInvite));
                        cnx.envoyer("Vous avez quitté le salon privé avec succès!");
                        serveur.envoyerMessagePrive(aliasInvite).envoyer("QUIT " + aliasExpediteur);
                    } else
                        cnx.envoyer("Le salon privé avec " + aliasInvite + " n'existe pas!");

                    break;

                default: //Renvoyer le texte recu convertit en majuscules :
                    msg = (evenement.getType() + " " + evenement.getArgument()).toUpperCase();
                    cnx.envoyer(msg);
            }
        }
    }
}