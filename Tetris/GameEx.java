package Test;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class GameEx extends Object {
    public static final int STATE_GETREADY = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_GAMEOVER = 4;

    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);
    private final SquareBoard board;
    private final SquareBoard previewBoard = new SquareBoard(5, 5);

    private Figure[] figures = {
            new Figure(Figure.SQUARE_FIGURE),
            new Figure(Figure.LINE_FIGURE),
            new Figure(Figure.S_FIGURE),
            new Figure(Figure.Z_FIGURE),
            new Figure(Figure.RIGHT_ANGLE_FIGURE),
            new Figure(Figure.LEFT_ANGLE_FIGURE),
            new Figure(Figure.TRIANGLE_FIGURE)
    };

    private final GameThread thread;
    private int level = 1;
    private int score = 0;
    private Figure figure = null;
    private Figure nextFigure = null;
    private int nextRotation = 0;
    private boolean preview = true;
    private boolean moveLock = false;
    private int state;

    public GameEx() {
        this(10, 20);
    }

    public GameEx(int width, int height) {
        board = new SquareBoard(width, height);
        thread = new GameThread();
        handleGetReady();
        board.getComponent().setFocusable(true);
        board.getComponent().addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                handleKeyEvent(e);
            }
        });
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }

    public int getState() {
        return state;
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public int getRemovedLines() {
        return board.getRemovedLines();
    }

    public Component getSquareBoardComponent() {
        return board.getComponent();
    }

    public Component getPreviewBoardComponent() {
        return previewBoard.getComponent();
    }

    public void init() {
        if (state == STATE_GAMEOVER) {
            handleGetReady();
        }
    }

    public void start() {
        handleStart();
    }

    public void pause() {
        if (state == STATE_PLAYING) {
            handlePause();
        }
    }

    public void resume() {
        if (state == STATE_PAUSED) {
            handleResume();
        }
    }

    public void terminate() {
        handleGameOver();
    }

    private void handleStart() {
        level = 1;
        score = 0;
        figure = null;
        nextFigure = randomFigure();
        nextFigure.rotateRandom();
        nextRotation = nextFigure.getRotation();

        state = STATE_PLAYING;
        board.setMessage(null);
        board.clear();
        previewBoard.clear();
        handleLevelModification();
        handleScoreModification();

        PCS.firePropertyChange("state", -1, STATE_PLAYING);
        thread.reset();
    }

    private void handleGameOver() {
        thread.setPaused(true);

        if (figure != null) {
            figure.detach();
        }
        figure = null;
        if (nextFigure != null) {
            nextFigure.detach();
        }
        nextFigure = null;

        state = STATE_GAMEOVER;
        board.setMessage("GAME OVER");
        PCS.firePropertyChange("state", -1, STATE_GAMEOVER);
    }

    private void handleGetReady() {
        board.setMessage("Play 버튼을 누르시오.");
        board.clear();
        previewBoard.clear();
        state = STATE_GETREADY;
        PCS.firePropertyChange("state", -1, STATE_GETREADY);
    }

    private void handlePause() {
        thread.setPaused(true);
        state = STATE_PAUSED;
        board.setMessage("일시 정지");
        PCS.firePropertyChange("state", -1, STATE_PAUSED);
    }

    private void handleResume() {
        state = STATE_PLAYING;
        board.setMessage(null);
        thread.setPaused(false);
        PCS.firePropertyChange("state", -1, STATE_PLAYING);
    }

    private void handleLevelModification() {
        PCS.firePropertyChange("level", -1, level);
        thread.adjustSpeed();
    }

    private void handleScoreModification() {
        PCS.firePropertyChange("score", -1, score);
    }

    private void handleFigureStart() {
        int rotation;

        figure = nextFigure;
        moveLock = false;
        rotation = nextRotation;
        nextFigure = randomFigure();
        nextFigure.rotateRandom();
        nextRotation = nextFigure.getRotation();

        if (preview) {
            previewBoard.clear();
            nextFigure.attach(previewBoard, true);
            nextFigure.detach();
        }

        figure.setRotation(rotation);
        if (!figure.attach(board, false)) {
            previewBoard.clear();
            figure.attach(previewBoard, true);
            figure.detach();
            handleGameOver();
        }
    }

    private void handleFigureLanded() {
        if (figure.isAllVisible()) {
            score += 10;
            handleScoreModification();
        } else {
            handleGameOver();
            return;
        }
        figure.detach();
        figure = null;

        if (board.hasFullLines()) {
            board.removeFullLines();
            PCS.firePropertyChange("lines", -1, board.getRemovedLines());
            if (level < 9 && board.getRemovedLines() / 20 > level) {
                level = board.getRemovedLines() / 20;
                handleLevelModification();
            }
        } else {
            handleFigureStart();
        }
    }

    private synchronized void handleTimer() {
        if (figure == null) {
            handleFigureStart();
        } else if (figure.hasLanded()) {
            handleFigureLanded();
        } else {
            figure.moveDown(); // 조각을 한 칸 아래로 이동
        }
    }

    private synchronized void handlePauseOnOff() {
        if (nextFigure == null) {
            handleStart();
        } else if (thread.isPaused()) {
            handleResume();
        } else {
            handlePause();
        }
    }

    private synchronized void handleKeyEvent(KeyEvent e) {
        if (state == STATE_GETREADY) {
            handleStart();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            handlePauseOnOff();
            return;
        }

        if (figure == null || moveLock || thread.isPaused()) {
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                figure.moveLeft();
                break;

            case KeyEvent.VK_RIGHT:
                figure.moveRight();
                break;

            case KeyEvent.VK_SPACE:
                if (state != STATE_GAMEOVER) { // 게임이 종료되지 않았을 때만 처리
                    try {
                        figure.moveAllWayDown();
                        moveLock = true; // 다음 움직임 잠금
                    } catch (Exception ex) {
                        // 예외 발생 시 처리 로직
                        System.err.println("Error moving figure down: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                break;

            case KeyEvent.VK_UP:
                figure.rotateCounterClockwise(); // 반시계 방향으로 회전
                break;

            case KeyEvent.VK_DOWN:
                figure.moveDown(); // 조각을 한 칸 아래로 이동
                break;

            case KeyEvent.VK_S:
                if (level < 9) {
                    level++;
                    handleLevelModification();
                }
                break;

            case KeyEvent.VK_N:
                preview = !preview;
                if (preview && figure != nextFigure) {
                    nextFigure.attach(previewBoard, true);
                    nextFigure.detach();
                } else {
                    previewBoard.clear();
                }
                break;
        }
    }

    private Figure randomFigure() {
        return figures[(int) (Math.random() * figures.length)];
    }

    private class GameThread extends Thread {
        private boolean paused = true;
        private int sleepTime = 500;

        public GameThread() {
        }

        public void reset() {
            adjustSpeed();
            setPaused(false);
            if (!isAlive()) {
                this.start();
            }
        }

        public boolean isPaused() {
            return paused;
        }

        public void setPaused(boolean paused) {
            this.paused = paused;
        }

        public void adjustSpeed() {
            sleepTime = 4500 / (level + 5) - 250;
            if (sleepTime < 50) {
                sleepTime = 50;
            }
        }

        public void run() {
            while (thread == this) {
                handleTimer();

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignore) {
                }

                while (paused && thread == this) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }
    }
}
