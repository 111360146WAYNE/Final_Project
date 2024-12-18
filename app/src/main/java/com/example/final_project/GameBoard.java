package com.example.final_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Random;

public class GameBoard extends BaseAdapter {
    private Context context;
    private int[][] board;
    private static final int BOARD_SIZE = 4;

    private int currentScore = 0;
    private int highScore = 0;
    private OnScoreChangeListener scoreChangeListener;

    public GameBoard(Context context) {
        this.context = context;
        board = new int[BOARD_SIZE][BOARD_SIZE];

        // 從SharedPreferences載入最高分
        SharedPreferences prefs = context.getSharedPreferences("2048_GAME", Context.MODE_PRIVATE);
        highScore = prefs.getInt("HIGH_SCORE", 0);
        initializeBoard();
    }

    public interface OnGameOverListener {
        void onGameOver(int finalScore);
        void onContinueGame();
    }

    private OnGameOverListener gameOverListener;

    // 設置遊戲結束監聽器
    public void setOnGameOverListener(OnGameOverListener listener) {
        this.gameOverListener = listener;
    }

    private boolean isGameOver() {
        // 檢查是否還有空白格子
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == 0) {
                    return false;  // 有空白格子，遊戲未結束
                }
            }
        }

        // 檢查是否還有可合併的相鄰方塊
        // 水平方向
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE - 1; col++) {
                if (board[row][col] == board[row][col + 1]) {
                    return false;  // 有可合併的方塊，遊戲未結束
                }
            }
        }

        // 垂直方向
        for (int col = 0; col < BOARD_SIZE; col++) {
            for (int row = 0; row < BOARD_SIZE - 1; row++) {
                if (board[row][col] == board[row + 1][col]) {
                    return false;  // 有可合併的方塊，遊戲未結束
                }
            }
        }

        // 如果沒有空白格子且沒有可合併的方塊，遊戲結束
        return true;
    }

    // 修改移動方法，加入遊戲結束檢測
    private void checkGameStatus() {
        if (isGameOver()) {
            // 遊戲結束
            if (gameOverListener != null) {
                gameOverListener.onGameOver(currentScore);
            }
        }
    }

    // 分數變化監聽器接口
    public interface OnScoreChangeListener {
        void onScoreChanged(int newScore);
        void onHighScoreChanged(int newHighScore);
    }

    // 設置分數變化監聽器
    public void setOnScoreChangeListener(OnScoreChangeListener listener) {
        this.scoreChangeListener = listener;
    }


    private void initializeBoard() {
        // 初始化遊戲板，添加兩個初始方塊
        addRandomTile();
        addRandomTile();
    }

    private void updateScore(int points) {
        currentScore += points;

        // 檢查是否創造了新的最高分
        if (currentScore > highScore) {
            highScore = currentScore;

            // 保存最高分到SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("2048_GAME", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("HIGH_SCORE", highScore);
            editor.apply();

            // 通知監聽器最高分發生變化
            if (scoreChangeListener != null) {
                scoreChangeListener.onHighScoreChanged(highScore);
            }
        }

        // 通知監聽器分數發生變化
        if (scoreChangeListener != null) {
            scoreChangeListener.onScoreChanged(currentScore);
        }
    }

    public void addRandomTile() {
        Random random = new Random();
        int value = random.nextInt(10) == 0 ? 4 : 2;

        while (true) {
            int row = random.nextInt(BOARD_SIZE);
            int col = random.nextInt(BOARD_SIZE);

            if (board[row][col] == 0) {
                board[row][col] = value;
                break;
            }
        }
    }

    public void moveLeft() {
        boolean moved = false;

        // 遍歷每一行
        for (int row = 0; row < BOARD_SIZE; row++) {
            // 第一步：壓縮非零元素
            int[] newRow = compressRow(board[row]);

            // 第二步：合併相同值的相鄰元素
            for (int col = 0; col < BOARD_SIZE - 1; col++) {
                if (newRow[col] != 0 && newRow[col] == newRow[col + 1]) {
                    newRow[col] *= 2;  // 合併，值加倍
                    newRow[col + 1] = 0;  // 下一個位置置零

                    // 更新分數邏輯可以在這裡添加
                    updateScore(newRow[col]);
                    // score += newRow[col];
                }
            }

            // 再次壓縮以填充空隙
            newRow = compressRow(newRow);

            // 檢查是否真的移動了
            if (!Arrays.equals(board[row], newRow)) {
                moved = true;
            }

            if (moved) {
                addRandomTile();
                checkGameStatus();  // 檢查遊戲狀態
            }


            // 更新遊戲板
            board[row] = newRow;
        }

        // 如果有移動，則添加新的隨機方塊
        if (moved) {
            addRandomTile();
        }

        // 更新UI
        notifyDataSetChanged();
    }

    public void moveRight() {
        boolean moved = false;

        // 遍歷每一行
        for (int row = 0; row < BOARD_SIZE; row++) {
            // 先反轉行，然後使用左移邏輯，最後再反轉回來
            int[] reversedRow = reverseRow(board[row]);
            int[] newRow = compressRow(reversedRow);

            // 合併相同值的相鄰元素
            for (int col = 0; col < BOARD_SIZE - 1; col++) {
                if (newRow[col] != 0 && newRow[col] == newRow[col + 1]) {
                    newRow[col] *= 2;
                    newRow[col + 1] = 0;

                    updateScore(newRow[col]);
                }
            }

            // 再次壓縮以填充空隙
            newRow = compressRow(newRow);

            // 反轉回原來的方向
            newRow = reverseRow(newRow);

            // 檢查是否真的移動了
            if (!Arrays.equals(board[row], newRow)) {
                moved = true;
            }

            if (moved) {
                addRandomTile();
                checkGameStatus();  // 檢查遊戲狀態
            }


            // 更新遊戲板
            board[row] = newRow;
        }

        // 如果有移動，則添加新的隨機方塊
        if (moved) {
            addRandomTile();
        }

        // 更新UI
        notifyDataSetChanged();
    }

    public void moveUp() {
        boolean moved = false;

        // 遍歷每一列
        for (int col = 0; col < BOARD_SIZE; col++) {
            // 獲取當前列
            int[] column = new int[BOARD_SIZE];
            for (int row = 0; row < BOARD_SIZE; row++) {
                column[row] = board[row][col];
            }

            // 壓縮列
            int[] newColumn = compressRow(column);

            // 合併相同值的相鄰元素
            for (int row = 0; row < BOARD_SIZE - 1; row++) {
                if (newColumn[row] != 0 && newColumn[row] == newColumn[row + 1]) {
                    newColumn[row] *= 2;
                    newColumn[row + 1] = 0;

                    updateScore(newColumn[col]);
                }
            }

            // 再次壓縮以填充空隙
            newColumn = compressRow(newColumn);

            // 檢查是否真的移動了
            boolean columnChanged = false;
            for (int row = 0; row < BOARD_SIZE; row++) {
                if (board[row][col] != newColumn[row]) {
                    columnChanged = true;
                    break;
                }
            }

            if (columnChanged) {
                moved = true;
            }

            // 更新遊戲板的這一列
            for (int row = 0; row < BOARD_SIZE; row++) {
                board[row][col] = newColumn[row];
            }
        }

        // 如果有移動，則添加新的隨機方塊
        if (moved) {
            addRandomTile();
        }

        if (moved) {
            addRandomTile();
            checkGameStatus();  // 檢查遊戲狀態
        }


        // 更新UI
        notifyDataSetChanged();
    }

    public void moveDown() {
        boolean moved = false;

        // 遍歷每一列
        for (int col = 0; col < BOARD_SIZE; col++) {
            // 獲取當前列
            int[] column = new int[BOARD_SIZE];
            for (int row = 0; row < BOARD_SIZE; row++) {
                column[row] = board[row][col];
            }

            // 反轉列，然後使用向上移動的邏輯
            int[] reversedColumn = reverseRow(column);
            int[] newColumn = compressRow(reversedColumn);

            // 合併相同值的相鄰元素
            for (int row = 0; row < BOARD_SIZE - 1; row++) {
                if (newColumn[row] != 0 && newColumn[row] == newColumn[row + 1]) {
                    newColumn[row] *= 2;
                    newColumn[row + 1] = 0;

                    updateScore(newColumn[col]);
                }
            }

            // 再次壓縮以填充空隙
            newColumn = compressRow(newColumn);

            // 反轉回原來的方向
            newColumn = reverseRow(newColumn);

            // 檢查是否真的移動了
            boolean columnChanged = false;
            for (int row = 0; row < BOARD_SIZE; row++) {
                if (board[row][col] != newColumn[row]) {
                    columnChanged = true;
                    break;
                }
            }

            if (columnChanged) {
                moved = true;
            }

            // 更新遊戲板的這一列
            for (int row = 0; row < BOARD_SIZE; row++) {
                board[row][col] = newColumn[row];
            }
        }

        // 如果有移動，則添加新的隨機方塊
        if (moved) {
            addRandomTile();
        }

        if (moved) {
            addRandomTile();
            checkGameStatus();  // 檢查遊戲狀態
        }

        // 更新UI
        notifyDataSetChanged();
    }

    // 輔助方法：壓縮一行/列，移除零值
    private int[] compressRow(int[] row) {
        // 刪除零值，並將非零值靠左移動
        int[] newRow = new int[BOARD_SIZE];
        int index = 0;

        for (int value : row) {
            if (value != 0) {
                newRow[index++] = value;
            }
        }

        return newRow;
    }

    // 輔助方法：反轉一行/列
    private int[] reverseRow(int[] row) {
        int[] reversedRow = new int[BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            reversedRow[i] = row[BOARD_SIZE - 1 - i];
        }
        return reversedRow;
    }

    @Override
    public int getCount() {
        return BOARD_SIZE * BOARD_SIZE;
    }

    @Override
    public Object getItem(int position) {
        int row = position / BOARD_SIZE;
        int col = position % BOARD_SIZE;
        return board[row][col];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 重置遊戲方法
    public void resetGame() {
        // 清空遊戲板
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                board[row][col] = 0;
            }
        }

        // 重置當前分數
        currentScore = 0;

        // 初始化遊戲板
        initializeBoard();

        // 通知監聽器分數變化
        if (scoreChangeListener != null) {
            scoreChangeListener.onScoreChanged(currentScore);
        }

        notifyDataSetChanged();
    }

    // 獲取當前分數
    public int getCurrentScore() {
        return currentScore;
    }

    // 獲取最高分
    public int getHighScore() {
        return highScore;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        if (convertView == null) {
            textView = new TextView(context);
            // 設置方塊樣式
        } else {
            textView = (TextView) convertView;
        }

        int row = position / BOARD_SIZE;
        int col = position % BOARD_SIZE;
        int value = board[row][col];

        textView.setText(value == 0 ? "" : String.valueOf(value));
        // 根據數值設置不同的背景顏色

        return textView;
    }
}