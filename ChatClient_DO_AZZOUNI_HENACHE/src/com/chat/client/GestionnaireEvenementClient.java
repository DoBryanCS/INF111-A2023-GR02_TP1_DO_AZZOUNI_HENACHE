package com.chat.client;

import com.chat.commun.evenement.Evenement;
import com.chat.commun.evenement.GestionnaireEvenement;
import com.chat.commun.net.Connexion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cette classe représente un gestionnaire d'événement d'un client. Lorsqu'un client reçoit un texte d'un serveur,
 * il crée un événement à partir du texte reçu et alerte ce gestionnaire qui réagit en gérant l'événement.
 *
 * @author Abdelmoumène Toudeft (Abdelmoumene.Toudeft@etsmtl.ca)
 * @version 1.0
 * @since 2023-09-01
 */
public class GestionnaireEvenementClient implements GestionnaireEvenement {
    private Client client;

    /**
     * Construit un gestionnaire d'événements pour un client.
     *
     * @param client Client Le client pour lequel ce gestionnaire gère des événements
     */
    public GestionnaireEvenementClient(Client client) {
        this.client = client;
    }
    /**
     * Méthode de gestion d'événements. Cette méthode contiendra le code qui gère les réponses obtenues d'un serveur.
     *
     * @param evenement L'événement à gérer.
     */
    @Override
    public void traiter(Evenement evenement) {
        Object source = evenement.getSource();
        Connexion cnx;
        String typeEvenement, arg, deplacement;
        String[] membres, messages, alias, arguments;
        EtatPartieEchecs etatPartieEchecs;

        if (source instanceof Connexion) {
            cnx = (Connexion) source;
            typeEvenement = evenement.getType();
            switch (typeEvenement) {
                case "END" : //Le serveur demande de fermer la connexion
                    client.deconnecter(); //On ferme la connexion
                    break;
                case "LIST" : //Le serveur a renvoyé la liste des connectés
                    arg = evenement.getArgument();
                    membres = arg.split(":");
                    System.out.println("\t\t"+membres.length+" personnes dans le salon :");
                    for (String s:membres)
                        System.out.println("\t\t\t- "+s);
                    break;
                case "HIST" : //Le serveur a a renvoyé la liste des messages publics
                    arg = evenement.getArgument();
                    messages = arg.split("\n");
                    if (messages[0].isEmpty()) {
                        System.out.println("\t\t" + "Il n'y a aucun message dans l'historique des messages publics!");
                    } else {
                        System.out.println("\t\t" + "Il y a " + messages.length + " messages dans l'historique des messages publics:");
                        for (String m : messages)
                            System.out.println("\t\t\t." + m);
                    }
                    break;
                case "JOIN" : //Informe un client de la réception d’une invitation à un chat privé
                    arg = evenement.getArgument();
                    System.out.println("\t\t" + "Vous avez recu une invitation de chat prive de " + arg + "!");
                    break;
                case "JOINOK" : //Valide le démarrage d’un chat privé avec alias.
                    arg = evenement.getArgument();
                    System.out.println("\t\t" + "Le salon de chat prive avec " + arg + " a ete cree!");
                    break;
                case "DECLINE" : //Informe le client que alias a refusé son invitation.
                    arg = evenement.getArgument();
                    System.out.println("\t\t" + arg + " a refuse votre invitation!");
                    break;
                case "INV" : //Envoie la liste des invitations à un chat privé.
                    arg = evenement.getArgument();
                    alias = arg.split(":");
                    if (alias[0].isEmpty()) {
                        System.out.println("\t\t" + "Vous n'avez aucune invitation A des chats prives!");
                    } else {
                        System.out.println("\t\t" + "Vous avez " + alias.length + " invitations à des chats prives:");
                        for (String a : alias)
                            System.out.println("\t\t\t." + a);
                    }
                    break;
                case "QUIT" : //Informe le client que alias a quitté le salon privé.
                    arg = evenement.getArgument();
                    System.out.println("\t\t" + arg + " a quitter le salon prive avec vous!");
                    break;
                case "CHESS" : //Informe le client de la réception d’une invitation à une partie d'échec.
                    arg = evenement.getArgument();
                    System.out.println("\t\t" + "Vous avez recu une invitation a une partie d'echec de " + arg + "!");
                    break;
                case "CHESSOK" : //Valide le démarrage d’une partie d’échecs pour les clients
                    arg = evenement.getArgument();
                    ((ClientChat) client).nouvellePartie();

                    etatPartieEchecs = ((ClientChat) client).getEtatPartieEchecs();
                    System.out.println(etatPartieEchecs);
                    System.out.println();

                    if (arg.equals("b"))
                        System.out.println("\t\t" + "Partie d'echec cree. Vous etes blanc, a vous de commencer!");
                    else
                        System.out.println("\t\t" + "Partie d'echec cree. Vous etes noir, attendez votre tour!");
                    break;
                case "MOVE" : //Valide un déplacement de pièce envoyé par un client.
                    ((ClientChat) client).gererMouvement(evenement.getArgument(), client);
                    break;
                case "INVALID" : //Invalide un déplacement de pièce envoyé par un client.
                    System.out.println("\t\t" + "Invalide, essayez un autre mouvement!");
                    break;
                case "ECHEC" : //Informe un client que son roi est en échec.
                    arg = evenement.getArgument();
                    arguments = arg.split("/");
                    deplacement = arguments[1];
                    ((ClientChat) client).gererMouvement(deplacement, client);
                    System.out.println("\t\t" + arguments[0] + " est en echec!");
                    break;
                case "MAT" : //Informe le client qu’il y a échec et mat et donne l’alias du gagnant.
                    arg = evenement.getArgument();
                    arguments = arg.split("/");
                    deplacement = arguments[1].replaceAll("[\\s-]", "");
                    ((ClientChat) client).gererMouvement(deplacement, client);
                    System.out.println("\t\t" + "Echec et mat, le gagnant est " + arguments[0] + "!");
                    ((ClientChat) client).setEtatPartieEchecs(null);
                    break;
                case "ABANDON" : //Abandonne une partie d’échecs.
                    arg = evenement.getArgument();
                    System.out.println("\t\t" + "Gagnant par abandon, le gagnant est " + arg + "!");
                    ((ClientChat) client).setEtatPartieEchecs(null);
                    break;
                default: //Afficher le texte recu :
                    System.out.println("\t\t\t."+evenement.getType()+" "+evenement.getArgument());
            }
        }
    }
}
