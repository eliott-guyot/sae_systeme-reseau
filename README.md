# Puissance 4 en Java

Ce projet implémente le jeu classique du Puissance 4 en Java, avec une architecture client-serveur permettant à deux joueurs de s'affronter à distance.

## Prérequis

*   Java Development Kit (JDK) 8 ou supérieur (testé avec JDK 17)
*   Un environnement de développement Java (IDE) comme IntelliJ IDEA ou Eclipse (recommandé, mais pas obligatoire)

## Installation

1.  Clonez le dépôt Git (si vous utilisez Git) :
    ```bash
    git clone <URL_du_repo>
    ```
2.  Compilez les fichiers Java. Vous pouvez le faire avec un IDE ou en utilisant la ligne de commande :
    ```bash
    javac -d bin src/*.java
    ```
    (Assurez-vous d'être dans le répertoire racine du projet)

## Comment lancer le jeu

Le jeu fonctionne avec une architecture client-serveur. Vous devez d'abord lancer le serveur, puis les clients.

1.  **Lancer le serveur :**
    ```bash
    java -cp bin Serveur
    ```
    Le serveur affichera un message indiquant qu'il est en écoute sur un port (par défaut, il utilise souvent le port 5000).

2.  **Lancer les clients :**
    Ouvrez un nouveau terminal pour chaque client et exécutez la commande suivante :
    ```bash
    java -cp bin Client <adresse_IP_du_serveur> <port_du_serveur>
    ```
    *   `<adresse_IP_du_serveur>` : L'adresse IP de la machine où le serveur est exécuté (par exemple, `127.0.0.1` pour localhost si le serveur est sur la même machine). Par défaut c'est `localhost`.
    *   `<port_du_serveur>` : Le port sur lequel le serveur écoute (par exemple, `5000`). Par défaut c'est `12345`.

    Exemple (si le serveur est sur la même machine) :
    ```bash
    java -cp bin Client
    ```
    Deux terminaux distincts devront exécuter cette commande pour que deux joueurs puissent se connecter.

## Comment jouer

Le but du Puissance 4 est d'aligner quatre jetons de sa couleur horizontalement, verticalement ou en diagonale. Les joueurs jouent à tour de rôle en choisissant une colonne où laisser tomber leur jeton.

## Structure du code

*   `Serveur.java` : Gère les connexions des clients et la logique du jeu.
*   `Client.java` : Interface utilisateur pour chaque joueur, communique avec le serveur.
*   `ClientHandler.java`: Gère la communication avec chaque client sur le serveur.
*   `Puissance4.java` : Contient la logique du jeu Puissance 4.
*   `ScoreManager.java` : Gère le stockage et la récupération des scores
*   `scores.json`: Fichier de sauvegarde des scores.

## Auteurs

*   [Eliott GUYOT](https://github.com/eliott-guyot) : Chef de projet
*   [Benjamin DORE](https://github.com/ArKoSs145)
*   [Mickael RIEGO](https://github.com/IckaeEtu)
*   [Nicolas NAUCHE](https://github.com/NicolasNauche)
