#  __Jeu du Puissance 4__ 
Le jeu se joue dans la console.
Un menu demande le nombre de joueur (1 ou 2), et leur nom.
Si l'utlisateur choisi 1 joueur alors il joue seul contre l'ordinateur.
Le premier joueur à aligner 4 jetons a gagné, si aucun joueur ne parvient à le faire alors c'est un match nul.



Mon code est divisé en cinq classes:
* Game
* Board
* Player
* Computer
* Position

Et une énumération:
* Symbol

#### La classe _Game_ gère le déroulement de la partie et les intéractions utilisateurs dans la console:

**_start()_** demande le nom de chaque joueur et le type de jeu (seul ou à deux) elle lance les méthodes initialize et playManche.

**_initialize()_** instancie les joueurs grâce aux noms passé en paramètre et tire aux hasard les symboles et le joueur qui commencera en utilisant la methode randomize.

**_playManche()_** annonce qui commencera, si la partie est gagné et par qui ou si il y a match nul, elle appelle la méthode jouerTourSolo tant que le jeu n'est pas fini. 

**_jouerTourSolo()_**  prend en paramètre un joueur et lui demande quelle colonne il veut jouer, vérifie sa disponibilié, et le fait jouer, ou fait jouer l'ordinateur.

**_isFinished()_** renvoi True si l'un des joueurs a gagné ou si il y a match nul ou False sinon.

**_randomize()__** renvoi 0 ou 1 pour choisir les couleurs ou la position des joueurs dans le tableu Joueur[].


#### La classe _Board_ contient la grille de jeu et les méthodes qui sont liées à cette dernière:

**_initializeGrid()_** rempli la grille de jeu de Symbol.VIDE qui est un point. 

**_isColumnFull()_** renvoi True si la colonne passé en paramètre est pleine ou False sinon. 

**_getEmptyPositionForColumn()_** renvoi une Position disponible pour la colonne passé en paramètre.

**_setSymbolAtPosition()_** attribut le symbol passé en paramètre à la position de la grille passé en paramètre. 


#### La classe _Player_ contient le nom du joueur, son nombre de jetons restant, les positions des jetons qu'il a joués et le symbole qu'il lui a été affecté:

**_playAtColumn()_** joue sur la colonne passé en paramètre.

**_hasWin()_** renvoi True si le joueur a quatre jeton alignés ou False sinon.

**_isPositionWinnable()_** renvoi True si une position peut remporter la victoire.

**_getConsecutivePositions()_** renvoi une HashMap avec pour clef la direction et pour valeur une liste de listes de positions consécutives.

**_decrementNumOfCoin()_** décrément le nombre de jetons du joueur.

#### La classe _Computer_ hérite des attributs et méthodes de _Player_ et contient des méthodes de gestion de l'IA:

**_definePositions()_** initialise les tableau de positions jouables et adverses.

**_getRandomPosition()_** renvoi une position aléatoire.

**_playAtPosition()_** joue à la position donnée en argument.

**_getPositionToPlay()_** renvoie la position à jouer la plus adaptée.

**_play()_** gère la mécanique de jeu de l'IA.

**_hasTwoCentralPositions()_** renvoi True si l'opposant a deux positions centrales consécutives ou False sinon.

#### L'énumération _Symbol_ définie les symboles par couleur de chaque jeton ou un symbole vide pour les cases vide:

**_JAUNE_** contient la valeur "X".

**_ROUGE_** contient la valeur "O".

**_VIDE_** contient la valeur ".".

#### La classe _Position_ représente les positions dans la grille de jeu avec ses attributs x et y.
