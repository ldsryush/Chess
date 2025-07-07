package chess;

import java.util.Arrays;

public class ChessBoard {
    private ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    public void addPiece(ChessPosition pos, ChessPiece piece) {
        board[pos.getRow() - 1][pos.getColumn() - 1] = piece;
    }
    public void removePiece(ChessPosition pos) {
        board[pos.getRow()-1][pos.getColumn()-1] = null;
    }

    public ChessPiece getPiece(ChessPosition pos) {
        return board[pos.getRow() - 1][pos.getColumn() - 1];
    }

    public void resetBoard() {
        board = new ChessPiece[8][8];
        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(2, col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

        ChessPiece.PieceType[] order = {
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        };

        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(1, col), new ChessPiece(ChessGame.TeamColor.WHITE, order[col - 1]));
            addPiece(new ChessPosition(8, col), new ChessPiece(ChessGame.TeamColor.BLACK, order[col - 1]));
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ChessBoard cb && Arrays.deepEquals(board, cb.board));
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}