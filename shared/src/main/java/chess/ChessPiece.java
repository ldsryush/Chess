package chess;

import java.util.*;

public class ChessPiece {
    private ChessGame.TeamColor color;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public enum PieceType { KING, QUEEN, BISHOP, KNIGHT, ROOK, PAWN }

    public ChessGame.TeamColor getTeamColor() { return color; }

    public PieceType getPieceType() { return type; }

    private boolean isValidPosition(ChessPosition pos) {
        int r = pos.getRow(), c = pos.getColumn();
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }

    private void tryAddMove(Collection<ChessMove> moves, ChessBoard board, ChessPosition from, ChessPosition to, boolean canPromote) {
        if (!isValidPosition(to)) return;
        ChessPiece target = board.getPiece(to);
        if (target == null || target.getTeamColor() != color) {
            if (canPromote && type == PieceType.PAWN) {
                for (var pt : new PieceType[]{PieceType.QUEEN, PieceType.KNIGHT, PieceType.ROOK, PieceType.BISHOP})
                    moves.add(new ChessMove(from, to, pt));
            } else {
                moves.add(new ChessMove(from, to, null));
            }
        }
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition pos) {
        Collection<ChessMove> moves = new HashSet<>();
        int r = pos.getRow(), c = pos.getColumn();
        int[] straight = {-1, 1};

        switch (type) {
            case PAWN -> {
                int dir = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
                int startRow = (color == ChessGame.TeamColor.WHITE) ? 2 : 7;
                int promoteRow = (color == ChessGame.TeamColor.WHITE) ? 8 : 1;

                ChessPosition oneStep = new ChessPosition(r + dir, c);
                if (isValidPosition(oneStep) && board.getPiece(oneStep) == null) {
                    tryAddMove(moves, board, pos, oneStep, oneStep.getRow() == promoteRow);
                    if (r == startRow) {
                        ChessPosition twoStep = new ChessPosition(r + 2 * dir, c);
                        if (board.getPiece(twoStep) == null)
                            tryAddMove(moves, board, pos, twoStep, false);
                    }
                }

                for (int dc : new int[]{-1, 1}) {
                    ChessPosition diag = new ChessPosition(r + dir, c + dc);
                    if (isValidPosition(diag)) {
                        ChessPiece target = board.getPiece(diag);
                        if (target != null && target.getTeamColor() != color)
                            tryAddMove(moves, board, pos, diag, diag.getRow() == promoteRow);
                    }
                }
            }
            case BISHOP -> generateDirectionalMoves(moves, board, pos, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
            case ROOK   -> generateDirectionalMoves(moves, board, pos, new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
            case QUEEN  -> generateDirectionalMoves(moves, board, pos, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}});
            case KNIGHT -> generateJumpMoves(moves, board, pos, new int[][]{{2,1},{1,2},{-1,2},{-2,1},{-2,-1},{-1,-2},{1,-2},{2,-1}});
            case KING   -> generateJumpMoves(moves, board, pos, new int[][]{{1,1},{1,0},{1,-1},{0,1},{0,-1},{-1,1},{-1,0},{-1,-1}});
        }

        return moves;
    }

    private void generateDirectionalMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos, int[][] directions) {
        for (int[] d : directions) {
            for (int i = 1; i < 8; i++) {
                ChessPosition next = new ChessPosition(pos.getRow() + d[0] * i, pos.getColumn() + d[1] * i);
                if (!isValidPosition(next)) break;
                ChessPiece target = board.getPiece(next);
                if (target == null) moves.add(new ChessMove(pos, next, null));
                else {
                    if (target.getTeamColor() != color) moves.add(new ChessMove(pos, next, null));
                    break;
                }
            }
        }
    }

    private void generateJumpMoves(Collection<ChessMove> moves, ChessBoard board, ChessPosition pos, int[][] deltas) {
        for (int[] d : deltas) {
            ChessPosition next = new ChessPosition(pos.getRow() + d[0], pos.getColumn() + d[1]);
            tryAddMove(moves, board, pos, next, false);
        }
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ChessPiece cp && color == cp.color && type == cp.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }
}