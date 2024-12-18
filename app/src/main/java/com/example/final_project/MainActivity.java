package com.example.final_project;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
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

        // 設置手勢檢測
        gestureDetector = new GestureDetector(this, new SwipeGestureListener(gameBoard));

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
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}