package com.example.final_project;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView scoreTextView;
    private TextView highScoreTextView;
    private GridView gridView;
    private GameBoard gameBoard;
    private GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreTextView = findViewById(R.id.scoreTextView);
        highScoreTextView = findViewById(R.id.highScoreTextView);
        gridView = findViewById(R.id.gridView);

        gameBoard = new GameBoard(this);
        gridView.setAdapter(gameBoard);

        // Use lambda for OnTouchListener
        gestureDetector = new GestureDetector(this, new SwipeGestureListener(gameBoard));
        gridView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        gameBoard.setOnScoreChangeListener(new GameBoard.OnScoreChangeListener() {
            @Override
            public void onScoreChanged(int newScore) {
                scoreTextView.setText("Score: " + newScore);
            }

            @Override
            public void onHighScoreChanged(int newHighScore) {
                highScoreTextView.setText("High Score: " + newHighScore);
            }
        });

        gameBoard.setOnGameOverListener(new GameBoard.OnGameOverListener() {
            @Override
            public void onGameOver(int finalScore) {
                showGameOverDialog(finalScore);
            }

            @Override
            public void onContinueGame() {
                // 如果需要在遊戲結束後繼續遊戲的邏輯
            }
        });


    }

    private void showGameOverDialog(int finalScore) {
        // 使用 LayoutInflater 來載入自定義佈局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_over, null);

        // 初始化佈局中的元件
        TextView finalScoreTextView = dialogView.findViewById(R.id.finalScoreTextView);
        Button restartButton = dialogView.findViewById(R.id.restartButton);

        // 設定分數
        finalScoreTextView.setText("您的最終分數：" + finalScore);

        // 創建對話框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // 設定背景透明，讓自定義背景生效
        AlertDialog gameOverDialog = builder.create();
        gameOverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // 設定按鈕點擊事件
        restartButton.setOnClickListener(v -> {
            gameOverDialog.dismiss();
            gameBoard.resetGame();
        });

        gameOverDialog.show();
    }
}