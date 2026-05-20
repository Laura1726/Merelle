# Fichiers de test — Merelle (Nine Men's Morris)
# ================================================

## Structure des fichiers

| Fichier                    | Scénario testé                                      |
|----------------------------|-----------------------------------------------------|
| test_placement_basic.txt   | Placement des 18 pions, transition vers MOVEMENT    |
| test_moulin_capture.txt    | Détection de moulin, PHASE_CAPTURE, règles capture  |
| test_movement_phase.txt    | Déplacements, annulation (C), fly, blocage          |
| test_end_conditions.txt    | Victoire < 3 pions, victoire blocage, nul 50 tours  |
| test_invalid_inputs.txt    | Robustesse : saisies invalides rejetées proprement  |

## Comment utiliser

Les fichiers de test servent de **scripts de saisie** pour le jeu en console.
Vous pouvez les passer en entrée standard via la redirection shell :

    java Main 0 < test_placement_basic.txt

Ou bien les utiliser comme **guide de jeu manuel** :
ouvrez le fichier, lisez chaque entrée et tapez-la dans le terminal.

## Conventions dans les fichiers

- Les lignes commençant par `#` sont des **commentaires** (pas des entrées).
- Chaque ligne non-commentaire est **une saisie** attendue par le jeu.
- `stop` met fin à la partie proprement (idWinner = -1).
- Les lignes vides simulent une frappe sur Entrée (test de robustesse).

## Mapping des positions (rappel)

     0-----------1-----------2
     |           |           |
     |   3-------4-------5   |
     |   |       |       |   |
     |   |   6---7---8   |   |
     |   |   |       |   |   |
     9--10--11      12--13--14
     |   |   |       |   |   |
     |   |  15--16--17   |   |
     |   |       |       |   |
     |  18------19------20   |
     |           |           |
    21----------22----------23

## Moulins valides (16 au total)

Horizontaux : {0,1,2} {3,4,5} {6,7,8} {9,10,11}
              {12,13,14} {15,16,17} {18,19,20} {21,22,23}
Verticaux   : {0,9,21} {3,10,18} {6,11,15} {1,4,7}
              {16,19,22} {8,12,17} {5,13,20} {2,14,23}

## Adjacences clés

- 0 ↔ 1, 9          - 7 ↔ 4, 6, 8, 16
- 1 ↔ 0, 2, 4       - 9 ↔ 0, 10, 21
- 4 ↔ 1, 3, 5, 7    - 10 ↔ 3, 9, 11, 18
- 16 ↔ 7, 15, 17, 19  - 22 ↔ 19, 21, 23

## Cas limites importants à vérifier

1. **Moulin persistant** : déplacer un pion hors d'un moulin puis le remettre
   ne doit PAS déclencher de capture (lastMillFormed bloque les moulins identiques).

2. **Capture protégée** : si tous les pions adverses sont en moulin,
   la capture est quand même autorisée (tous deviennent capturables).

3. **Fly** : un joueur réduit à exactement 3 pions en MOVEMENT peut
   se déplacer vers n'importe quelle case vide.

4. **DRAW_LIMIT** : exactement 50 tours consécutifs SANS capture déclenchent
   le nul (noCaptureTurns >= 50). Une capture remet le compteur à 0.
