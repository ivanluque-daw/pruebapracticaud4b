package org.iesvdm.sudoku;

//import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class SudokuTest {
    @Test
    public void testFillBoardRandomly() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardRandomly();

        for (int i = 0; i < sudoku.getGridSize(); i++) {
            for (int j = 0; j < sudoku.getGridSize(); j++) {
                Assertions.assertThat(sudoku.getBoard()[i][j]).isInstanceOf(Integer.class);
            }
        }
    }

    @Test
    public void testFillBoardBasedInCluesRandomly() {
        Sudoku sudoku = new Sudoku();
        sudoku.setNumClues(81);
        sudoku.fillBoardBasedInCluesRandomly();

        for (int i = 0; i < sudoku.getGridSize(); i++) {
            for (int j = 0; j < sudoku.getGridSize(); j++) {
                Assertions.assertThat(sudoku.getBoard()[i][j]).isInstanceOf(Integer.class);
                Assertions.assertThat(sudoku.getBoard()[i][j]).isNotZero();
            }
        }
    }

    @Test
    public void testPutNumberInBoard() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardRandomly();
        sudoku.putNumberInBoard(2, 1, 4);

        Assertions.assertThat(sudoku.getBoard()[1][4]).isEqualTo(2);
    }

    @Test
    public void testFillBoardBasedInCluesRandomlySolvable() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardBasedInCluesRandomlySolvable();

        // Aqui no deberia fallar pero en la condicion del while debería comprobar que no se resuelva
        Assertions.assertThat(sudoku.solveBoard()).isTrue();
    }

    @Test
    public void testFillBoardSolvable() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardSolvable();

        Assertions.assertThat(sudoku.solveBoard()).isTrue();
    }

    @Test
    public void testFillBoardUnsolvable() {
        Sudoku sudoku = new Sudoku();
        sudoku.fillBoardUnsolvable();

        Assertions.assertThat(sudoku.solveBoard()).isFalse();
    }

    @Test
    public void testIsNumberInRow() {
        int[][] board = new int[][]{{1, 2}, {1, 2}};
        Sudoku sudoku = new Sudoku();
        sudoku.setBoard(board);
        sudoku.setGridSize(2);

        assertThat(sudoku.isNumberInRow(1, 1)).isTrue();
        assertThat(sudoku.isNumberInRow(3, 0)).isFalse();
    }

    @Test
    public void testIsNumberInColumn() {
        int[][] board = new int[][]{{1, 2}, {1, 2}};
        Sudoku sudoku = new Sudoku();
        sudoku.setBoard(board);
        sudoku.setGridSize(2);

        assertThat(sudoku.isNumberInColumn(2, 1)).isTrue();
        assertThat(sudoku.isNumberInColumn(1, 1)).isFalse();
    }

    @Test
    public void testIsNumberInBox() {
        int[][] board = new int[][]{{1, 2, 3}, {1, 4, 3}, {1, 2, 3, 5}};
        Sudoku sudoku = new Sudoku();
        sudoku.setBoard(board);
        sudoku.setGridSize(3);

        assertThat(sudoku.isNumberInBox(4, 1, 1)).isTrue();
        assertThat(sudoku.isNumberInBox(5, 1, 1)).isFalse();
    }

    @Test
    public void testIsValidPlacement() {
        int[][] board = new int[][]{{1, 2, 3}, {1, 4, 3}, {1, 2, 3}};
        Sudoku sudoku = new Sudoku();
        sudoku.setBoard(board);
        sudoku.setGridSize(3);

        assertThat(sudoku.isValidPlacement(5, 0, 0)).isTrue();
        assertThat(sudoku.isValidPlacement(3, 0, 0)).isFalse();
    }

    @Test
    public void testSolveBoard() {
        int[][] b1 = new int[][]{{1, 2, 3}, {1, 4, 3}, {1, 2, 3}};
        int[][] b2 = new int[][]{{1, 2, 3}, {1, 0, 3}, {1, 2, 3}};

        Sudoku s1 = new Sudoku();
        s1.setBoard(b1);
        s1.setGridSize(3);

        Sudoku s2 = new Sudoku();
        s2.setBoard(b2);
        s2.setGridSize(3);

        assertThat(s1.solveBoard()).isTrue();
        assertThat(s2.solveBoard()).isFalse();
    }
}
