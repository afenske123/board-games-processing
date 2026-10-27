# Brettspiele — Processing-Oberfläche

Tic-Tac-Toe, Schach und Vier gewinnt mit einer nativen [Processing](https://processing.org/)-
Oberfläche, als zweite, eigenständige Darstellungsschicht neben der browserbasierten
Variante im Projekt [`live-view-engine`](../live-view-engine/).

![Java](https://img.shields.io/badge/Java-21-orange) ![Processing](https://img.shields.io/badge/GUI-Processing-9b5de5)

## Idee

Die eigentliche Spiellogik (Spielregeln, Zugvalidierung, Zobrist-Hashing) und die
KI (Minimax mit Alpha-Beta-Suche und Transpositionstabelle) sind komplett von der
Darstellung getrennt (`core`/`ai`/`games`) — genau dieselben Klassen treiben in
`live-view-engine` die Browser-Ansicht an. Für dieses Projekt wurden sie 1:1
übernommen (Paket `de.fenske.liveview.*` → `de.fenske.boardgames.*`), damit hier nur
noch eine neue Präsentationsschicht (`gui/BoardGamesSketch.java`) mit Processing
darüber gebaut werden musste, statt Spiellogik und KI ein zweites Mal zu schreiben —
so wie in echten Projekten dieselbe Anwendungslogik oft mehrere Oberflächen bedient.

## Starten

```bash
./gradlew run
```

Auf dem Startbildschirm mit `1`/`2`/`3` ein Spiel wählen. Mit der Maus klicken, um zu
ziehen (bei Schach: erst die eigene Figur anklicken, sie zeigt die legalen Zielfelder,
dann das Zielfeld anklicken); danach zieht der Computer automatisch. `R` startet das
aktuelle Spiel neu, `ESC`/Rücktaste geht zurück ins Menü.

## Processing core.jar besorgen

Dieses Projekt bindet Processing als lokale JAR ein (kein öffentliches Maven-Artefakt
nötig):

1. [Processing](https://processing.org/download) herunterladen und einmal entpacken.
2. Darin die Datei `core/library/core.jar` (Windows/Linux) bzw. den entsprechenden Pfad
   im `.app`-Bundle (macOS) suchen.
3. Nach `libs/core.jar` in diesem Projekt kopieren.

## Ausblick: Web-Variante

Da Spiellogik und KI hier komplett unabhängig von der Oberfläche sind, ließe sich
genauso gut eine dritte Präsentationsschicht ergänzen, z. B. mit HTML/CSS/TypeScript
als eigenständige Webanwendung (ohne den in `live-view-engine` verwendeten
Java-Server) — die `core`/`ai`/`games`-Pakete blieben dabei unverändert.
