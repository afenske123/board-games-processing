package de.fenske.boardgames.gui;

import de.fenske.boardgames.ai.Agent;
import de.fenske.boardgames.core.GameState;
import de.fenske.boardgames.core.Move;
import de.fenske.boardgames.games.Chess;
import de.fenske.boardgames.games.Connect4;
import de.fenske.boardgames.games.TicTacToe;
import processing.core.PApplet;
import processing.core.PFont;

/**
 * Processing-Oberflaeche fuer Tic-Tac-Toe, Schach und Vier gewinnt -- als zweite,
 * eigenstaendige Praesentationsschicht neben der browserbasierten Variante in
 * {@code live-view-engine}. Beide Varianten teilen sich dieselbe Spiellogik und
 * dieselbe Alpha-Beta-Suchengine ({@code core}/{@code ai}/{@code games}, hier 1:1
 * aus {@code live-view-engine} uebernommen und nur umbenannt), es wurde also
 * bewusst nicht "zwei Mal alles neu geschrieben", sondern nur die Darstellung
 * ausgetauscht -- so wie es auch im richtigen Leben zwei UIs (Web und Desktop)
 * fuer dieselbe Anwendungslogik gaebe.
 *
 * <p>Bedienung: Auf dem Startbildschirm 1/2/3 druecken, um ein Spiel zu waehlen.
 * Klick auf ein Feld (Tic-Tac-Toe/Schach) bzw. eine Spalte (Vier gewinnt) zieht;
 * danach zieht der Computer (Alpha-Beta-Suche) automatisch. Bei Schach: Klick auf
 * eine eigene Figur zeigt alle legalen Zielfelder, ein zweiter Klick zieht dorthin.
 * R setzt das aktuelle Spiel zurueck, ESC (bzw. Backspace) geht zurueck ins Menue.
 */
public class BoardGamesSketch extends PApplet {

    private enum Screen { MENU, TICTACTOE, CHESS, CONNECT4 }

    private static final int CELL = 90;
    private static final int BOARD_TOP = 70;
    private static final int BOARD_LEFT = 40;

    private Screen screen = Screen.MENU;
    private PFont font;
    private String message = "";

    // Tic-Tac-Toe
    private TicTacToe ttt;
    private Agent tttComputer;
    private static final int TTT_HUMAN = 0, TTT_COMPUTER = 1;

    // Vier gewinnt
    private Connect4 c4;
    private Agent c4Computer;
    private static final int C4_HUMAN = 0, C4_COMPUTER = 1;

    // Schach
    private Chess chess;
    private Agent chessComputer;
    private static final int CHESS_HUMAN = 0, CHESS_COMPUTER = 1;
    private int selectedRow = -1, selectedCol = -1;

    public static void main(String[] args) {
        PApplet.main(BoardGamesSketch.class.getName());
    }

    @Override
    public void settings() {
        size(720, 700);
    }

    @Override
    public void setup() {
        surface.setTitle("Brettspiele — Processing (Tic-Tac-Toe / Schach / Vier gewinnt)");
        font = createFont("SansSerif", 18);
        textFont(font);
        tttComputer = new Agent.AlphaBetaAgent(9, true);   // TicTacToe: voller Suchbaum
        c4Computer = new Agent.AlphaBetaAgent(7, true);    // Vier gewinnt: tiefenbegrenzt (evaluate())
        chessComputer = new Agent.AlphaBetaAgent(3, true); // Schach: tiefenbegrenzt (evaluate())
        resetTicTacToe();
        resetConnect4();
        resetChess();
    }

    @Override
    public void draw() {
        background(28, 30, 36);
        switch (screen) {
            case MENU -> drawMenu();
            case TICTACTOE -> drawTicTacToe();
            case CHESS -> drawChess();
            case CONNECT4 -> drawConnect4();
        }
    }

    // ------------------------------------------------------------------
    // Menue
    // ------------------------------------------------------------------

    private void drawMenu() {
        fill(230);
        textSize(28);
        text("Brettspiele (Processing)", 40, 60);
        textSize(18);
        fill(180, 200, 255);
        text("1 — Tic-Tac-Toe", 40, 140);
        text("2 — Schach", 40, 175);
        text("3 — Vier gewinnt", 40, 210);
        fill(140);
        textSize(14);
        text("Alle drei nutzen dieselbe Alpha-Beta-Suchengine (core/ai/games),", 40, 260);
        text("die auch die Browser-Variante in live-view-engine antreibt.", 40, 280);
    }

    @Override
    public void keyPressed() {
        if (screen == Screen.MENU) {
            if (key == '1') { screen = Screen.TICTACTOE; message = ""; }
            else if (key == '2') { screen = Screen.CHESS; message = ""; }
            else if (key == '3') { screen = Screen.CONNECT4; message = ""; }
            return;
        }
        if (key == ESC || keyCode == BACKSPACE) {
            key = 0; // verhindert, dass Processing den Sketch bei ESC schliesst
            screen = Screen.MENU;
            return;
        }
        if (key == 'r' || key == 'R') {
            switch (screen) {
                case TICTACTOE -> resetTicTacToe();
                case CHESS -> resetChess();
                case CONNECT4 -> resetConnect4();
                default -> { }
            }
        }
    }

    @Override
    public void mousePressed() {
        switch (screen) {
            case TICTACTOE -> handleTicTacToeClick();
            case CHESS -> handleChessClick();
            case CONNECT4 -> handleConnect4Click();
            default -> { }
        }
    }

    private void drawFooter(String hint) {
        fill(140);
        textSize(13);
        text(hint + "   |   R = neues Spiel   |   ESC/Backspace = Menue", 20, height - 20);
        fill(255, 210, 120);
        textSize(15);
        if (!message.isEmpty()) text(message, 20, height - 45);
    }

    // ------------------------------------------------------------------
    // Tic-Tac-Toe
    // ------------------------------------------------------------------

    private void resetTicTacToe() {
        ttt = new TicTacToe();
        message = "Du spielst X. Klicke ein Feld.";
    }

    private void drawTicTacToe() {
        int top = 110;
        int left = (width - 3 * CELL) / 2;
        stroke(90, 96, 110);
        strokeWeight(2);
        for (int i = 1; i < 3; i++) {
            line(left + i * CELL, top, left + i * CELL, top + 3 * CELL);
            line(left, top + i * CELL, left + 3 * CELL, top + i * CELL);
        }
        textSize(48);
        textAlign(CENTER, CENTER);
        for (int c = 0; c < 9; c++) {
            int row = c / 3, col = c % 3;
            int cx = left + col * CELL + CELL / 2;
            int cy = top + row * CELL + CELL / 2;
            int v = ttt.at(c);
            if (v == 0) { fill(120, 190, 255); text("X", cx, cy); }
            else if (v == 1) { fill(255, 140, 140); text("O", cx, cy); }
        }
        textAlign(LEFT, BASELINE);
        fill(230);
        textSize(24);
        text("Tic-Tac-Toe", 40, 50);

        if (ttt.isTerminal()) {
            int w = ttt.winner();
            message = (w == -1) ? "Unentschieden." : (w == TTT_HUMAN ? "Du gewinnst!" : "Computer gewinnt.");
        }
        drawFooter("Klick auf ein freies Feld, um zu ziehen");
    }

    private void handleTicTacToeClick() {
        if (ttt.isTerminal() || ttt.playerToMove() != TTT_HUMAN) return;
        int top = 110;
        int left = (width - 3 * CELL) / 2;
        int col = (mouseX - left) / CELL;
        int row = (mouseY - top) / CELL;
        if (col < 0 || col > 2 || row < 0 || row > 2) return;
        int cell = row * 3 + col;
        if (ttt.at(cell) != TicTacToe.EMPTY) return;

        ttt = (TicTacToe) ttt.apply(new TicTacToe.Cell(cell));
        maybeComputerMoveTicTacToe();
    }

    private void maybeComputerMoveTicTacToe() {
        if (ttt.isTerminal() || ttt.playerToMove() != TTT_COMPUTER) return;
        Move m = tttComputer.chooseMove(ttt);
        ttt = (TicTacToe) ttt.apply(m);
    }

    // ------------------------------------------------------------------
    // Vier gewinnt
    // ------------------------------------------------------------------

    private void resetConnect4() {
        c4 = new Connect4();
        message = "Du spielst X. Klicke eine Spalte.";
    }

    private void drawConnect4() {
        int top = 110;
        int left = (width - Connect4.WIDTH * 70) / 2;
        int cell = 70;
        fill(40, 70, 160);
        rect(left, top, Connect4.WIDTH * cell, Connect4.HEIGHT * cell, 8);
        for (int c = 0; c < Connect4.WIDTH; c++) {
            for (int r = 0; r < Connect4.HEIGHT; r++) {
                int cx = left + c * cell + cell / 2;
                int cy = top + (Connect4.HEIGHT - 1 - r) * cell + cell / 2;
                int v = c4.at(c, r);
                if (v == 0) fill(255, 210, 60);
                else if (v == 1) fill(230, 60, 60);
                else fill(20, 30, 60);
                ellipse(cx, cy, cell - 10, cell - 10);
            }
        }
        fill(230);
        textSize(24);
        text("Vier gewinnt", 40, 50);

        if (c4.isTerminal()) {
            int w = c4.utility(C4_HUMAN);
            message = (w == 0 && c4.legalMoves().isEmpty()) ? "Unentschieden."
                    : (w > 0 ? "Du gewinnst!" : (w < 0 ? "Computer gewinnt." : "Unentschieden."));
        }
        drawFooter("Klick auf eine Spalte, um einzuwerfen");
    }

    private void handleConnect4Click() {
        if (c4.isTerminal() || c4.playerToMove() != C4_HUMAN) return;
        int top = 110;
        int cellSize = 70;
        int left = (width - Connect4.WIDTH * cellSize) / 2;
        int col = (mouseX - left) / cellSize;
        if (col < 0 || col >= Connect4.WIDTH) return;
        boolean legal = c4.legalMoves().stream().anyMatch(m -> ((Connect4.Drop) m).column() == col);
        if (!legal) return;

        c4 = (Connect4) c4.apply(new Connect4.Drop(col));
        maybeComputerMoveConnect4();
    }

    private void maybeComputerMoveConnect4() {
        if (c4.isTerminal() || c4.playerToMove() != C4_COMPUTER) return;
        Move m = c4Computer.chooseMove(c4);
        c4 = (Connect4) c4.apply(m);
    }

    // ------------------------------------------------------------------
    // Schach
    // ------------------------------------------------------------------

    private void resetChess() {
        chess = new Chess();
        selectedRow = -1;
        selectedCol = -1;
        message = "Du spielst Weiss. Klicke eine Figur, dann ein Zielfeld.";
    }

    private void drawChess() {
        int top = 90;
        int left = (width - 8 * CELL) / 2;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                fill(light ? color(235, 224, 200) : color(90, 70, 55));
                int y = top + (7 - r) * CELL;
                rect(left + c * CELL, y, CELL, CELL);
            }
        }
        if (selectedRow != -1) {
            fill(255, 235, 120, 140);
            rect(left + selectedCol * CELL, top + (7 - selectedRow) * CELL, CELL, CELL);
            for (Move mv : chess.legalMoves()) {
                Chess.ChessMove cm = (Chess.ChessMove) mv;
                if (cm.fromRow() == selectedRow && cm.fromCol() == selectedCol) {
                    fill(120, 255, 140, 140);
                    ellipse(left + cm.toCol() * CELL + CELL / 2f, top + (7 - cm.toRow()) * CELL + CELL / 2f, 22, 22);
                }
            }
        }

        textSize(48);
        textAlign(CENTER, CENTER);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int piece = chess.at(r, c);
                if (piece == Chess.EMPTY) continue;
                fill(piece > 0 ? color(250) : color(20));
                int y = top + (7 - r) * CELL + CELL / 2;
                text(pieceGlyph(piece), left + c * CELL + CELL / 2, y);
            }
        }
        textAlign(LEFT, BASELINE);
        fill(230);
        textSize(24);
        text("Schach", 40, 50);

        if (chess.isTerminal()) {
            boolean humanInCheck = chess.isInCheck(CHESS_HUMAN);
            boolean computerInCheck = chess.isInCheck(CHESS_COMPUTER);
            if (humanInCheck) message = "Matt — Computer gewinnt.";
            else if (computerInCheck) message = "Matt — Du gewinnst!";
            else message = "Patt — Unentschieden.";
        }
        drawFooter("Klick auf eine eigene Figur, dann auf ein markiertes Zielfeld");
    }

    /** Einfache Unicode-Schachsymbole (kein Bild-Asset noetig). */
    private String pieceGlyph(int piece) {
        int type = Math.abs(piece);
        boolean white = piece > 0;
        return switch (type) {
            case Chess.PAWN -> white ? "♙" : "♟";
            case Chess.KNIGHT -> white ? "♘" : "♞";
            case Chess.BISHOP -> white ? "♗" : "♝";
            case Chess.ROOK -> white ? "♖" : "♜";
            case Chess.QUEEN -> white ? "♕" : "♛";
            case Chess.KING -> white ? "♔" : "♚";
            default -> "";
        };
    }

    private void handleChessClick() {
        if (chess.isTerminal() || chess.playerToMove() != CHESS_HUMAN) return;
        int top = 90;
        int left = (width - 8 * CELL) / 2;
        int col = (mouseX - left) / CELL;
        int rowFromTop = (mouseY - top) / CELL;
        if (col < 0 || col > 7 || rowFromTop < 0 || rowFromTop > 7) return;
        int row = 7 - rowFromTop;

        if (selectedRow == -1) {
            int piece = chess.at(row, col);
            if (piece != Chess.EMPTY && (piece > 0) == (CHESS_HUMAN == 0)) {
                selectedRow = row;
                selectedCol = col;
            }
            return;
        }

        Chess.ChessMove chosen = null;
        for (Move mv : chess.legalMoves()) {
            Chess.ChessMove cm = (Chess.ChessMove) mv;
            if (cm.fromRow() == selectedRow && cm.fromCol() == selectedCol
                    && cm.toRow() == row && cm.toCol() == col) {
                chosen = cm;
                break;
            }
        }
        if (chosen != null) {
            chess = (Chess) chess.apply(chosen);
            selectedRow = -1;
            selectedCol = -1;
            maybeComputerMoveChess();
        } else {
            // Neu auswaehlen, falls auf eine andere eigene Figur geklickt wurde
            int piece = chess.at(row, col);
            selectedRow = -1;
            selectedCol = -1;
            if (piece != Chess.EMPTY && (piece > 0) == (CHESS_HUMAN == 0)) {
                selectedRow = row;
                selectedCol = col;
            }
        }
    }

    private void maybeComputerMoveChess() {
        if (chess.isTerminal() || chess.playerToMove() != CHESS_COMPUTER) return;
        Move m = chessComputer.chooseMove(chess);
        chess = (Chess) chess.apply(m);
    }
}
