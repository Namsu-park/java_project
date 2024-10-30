package Test;

import java.awt.*;

public class Figure extends Object {

    public static final int SQUARE_FIGURE = 1;

    public static final int LINE_FIGURE = 2;

    public static final int S_FIGURE = 3;

    public static final int Z_FIGURE = 4;

    public static final int RIGHT_ANGLE_FIGURE = 5;

    public static final int LEFT_ANGLE_FIGURE = 6;

    public static final int TRIANGLE_FIGURE = 7;

    private SquareBoard board = null;

    private int xPos = 0;

    private int yPos = 0;

    private int orientation = 0;

    private int maxOrientation = 4;

    private int[] shapeX = new int[4];

    private int[] shapeY = new int[4];

    private Color color = Color.white;

    public Figure(int type) throws IllegalArgumentException {
        initialize(type);
    }

    private void initialize(int type) throws IllegalArgumentException {

        // 기본 변수 초기화
        board = null;
        xPos = 0;
        yPos = 0;
        orientation = 0;

        // 도형 유형 변수 초기화
        switch (type) {
            case SQUARE_FIGURE:
                maxOrientation = 1;
                color = Configuration.getColor("figure.square", "#ffd8b1");
                shapeX[0] = -1;
                shapeY[0] = 0;
                shapeX[1] = 0;
                shapeY[1] = 0;
                shapeX[2] = -1;
                shapeY[2] = 1;
                shapeX[3] = 0;
                shapeY[3] = 1;
                break;
            case LINE_FIGURE:
                maxOrientation = 2;
                color = Configuration.getColor("figure.line", "#ffb4b4");
                shapeX[0] = -2;
                shapeY[0] = 0;
                shapeX[1] = -1;
                shapeY[1] = 0;
                shapeX[2] = 0;
                shapeY[2] = 0;
                shapeX[3] = 1;
                shapeY[3] = 0;
                break;
            case S_FIGURE:
                maxOrientation = 2;
                color = Configuration.getColor("figure.s", "#a3d5ee");
                shapeX[0] = 0;
                shapeY[0] = 0;
                shapeX[1] = 1;
                shapeY[1] = 0;
                shapeX[2] = -1;
                shapeY[2] = 1;
                shapeX[3] = 0;
                shapeY[3] = 1;
                break;
            case Z_FIGURE:
                maxOrientation = 2;
                color = Configuration.getColor("figure.z", "#f4adff");
                shapeX[0] = -1;
                shapeY[0] = 0;
                shapeX[1] = 0;
                shapeY[1] = 0;
                shapeX[2] = 0;
                shapeY[2] = 1;
                shapeX[3] = 1;
                shapeY[3] = 1;
                break;
            case RIGHT_ANGLE_FIGURE:
                maxOrientation = 4;
                color = Configuration.getColor("figure.right", "#c0b6fa");
                shapeX[0] = -1;
                shapeY[0] = 0;
                shapeX[1] = 0;
                shapeY[1] = 0;
                shapeX[2] = 1;
                shapeY[2] = 0;
                shapeX[3] = 1;
                shapeY[3] = 1;
                break;
            case LEFT_ANGLE_FIGURE:
                maxOrientation = 4;
                color = Configuration.getColor("figure.left", "#f5f4a7");
                shapeX[0] = -1;
                shapeY[0] = 0;
                shapeX[1] = 0;
                shapeY[1] = 0;
                shapeX[2] = 1;
                shapeY[2] = 0;
                shapeX[3] = -1;
                shapeY[3] = 1;
                break;
            case TRIANGLE_FIGURE:
                maxOrientation = 4;
                color = Configuration.getColor("figure.triangle", "#a4d9b6");
                shapeX[0] = -1;
                shapeY[0] = 0;
                shapeX[1] = 0;
                shapeY[1] = 0;
                shapeX[2] = 1;
                shapeY[2] = 0;
                shapeX[3] = 0;
                shapeY[3] = 1;
                break;
            default:
                throw new IllegalArgumentException("No figure constant: " +
                        type);
        }
    }

    public boolean isAttached() {
        return board != null;
    }

    public boolean attach(SquareBoard board, boolean center) {
        int newX;
        int newY;
        int i;

        // 이전 연결 여부 확인
        if (isAttached()) {
            detach();
        }

        // 위치 초기화 (올바른 조작을 위해)
        xPos = 0;
        yPos = 0;

        // 위치 계산
        newX = board.getBoardWidth() / 2;
        if (center) {
            newY = board.getBoardHeight() / 2;
        } else {
            newY = 0;
            for (i = 0; i < shapeX.length; i++) {
                if (getRelativeY(i, orientation) - newY > 0) {
                    newY = -getRelativeY(i, orientation);
                }
            }
        }

        // 위치 확인
        this.board = board;
        if (!canMoveTo(newX, newY, orientation)) {
            this.board = null;
            return false;
        }

        // 도형 그리기
        xPos = newX;
        yPos = newY;
        paint(color);
        board.update();

        return true;
    }

    public void detach() {
        board = null;
    }

    public boolean isAllVisible() {
        if (!isAttached()) {
            return false;
        }
        for (int i = 0; i < shapeX.length; i++) {
            if (yPos + getRelativeY(i, orientation) < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean hasLanded() {
        return !isAttached() || !canMoveTo(xPos, yPos + 1, orientation);
    }

    public void moveLeft() {
        if (isAttached() && canMoveTo(xPos - 1, yPos, orientation)) {
            paint(null);
            xPos--;
            paint(color);
            board.update();
        }
    }

    public void moveRight() {
        if (isAttached() && canMoveTo(xPos + 1, yPos, orientation)) {
            paint(null);
            xPos++;
            paint(color);
            board.update();
        }
    }

    public void moveDown() {
        if (isAttached() && canMoveTo(xPos, yPos + 1, orientation)) {
            paint(null);
            yPos++;
            paint(color);
            board.update();
        }
    }

    public void moveAllWayDown() {
        int y = yPos;

        // 보드 확인
        if (!isAttached()) {
            return;
        }

        // 가장 낮은 위치 찾기
        while (canMoveTo(xPos, y + 1, orientation)) {
            y++;
        }

        // 업데이트
        if (y != yPos) {
            paint(null);
            yPos = y;
            paint(color);
            board.update();
        }
    }

    public int getRotation() {
        return orientation;
    }

    public void setRotation(int rotation) {
        int newOrientation;

        // 새로운 방향 설정
        newOrientation = rotation % maxOrientation;

        // 새 위치 확인
        if (!isAttached()) {
            orientation = newOrientation;
        } else if (canMoveTo(xPos, yPos, newOrientation)) {
            paint(null);
            orientation = newOrientation;
            paint(color);
            board.update();
        }
    }

    public void rotateRandom() {
        setRotation((int) (Math.random() * 4.0) % maxOrientation);
    }

    public void rotateClockwise() {
        if (maxOrientation == 1) {
            return;
        } else {
            setRotation((orientation + 1) % maxOrientation);
        }
    }

    public void rotateCounterClockwise() {
        if (maxOrientation == 1) {
            return;
        } else {
            setRotation((orientation + 3) % 4);
        }
    }

    private boolean isInside(int x, int y) {
        for (int i = 0; i < shapeX.length; i++) {
            if (x == xPos + getRelativeX(i, orientation)
                    && y == yPos + getRelativeY(i, orientation)) {

                return true;
            }
        }
        return false;
    }

    private boolean canMoveTo(int newX, int newY, int newOrientation) {
        int x;
        int y;

        for (int i = 0; i < 4; i++) {
            x = newX + getRelativeX(i, newOrientation);
            y = newY + getRelativeY(i, newOrientation);
            if (!isInside(x, y) && !board.isSquareEmpty(x, y)) {
                return false;
            }
        }
        return true;
    }

    private int getRelativeX(int square, int orientation) {
        switch (orientation % 4) {
            case 0:
                return shapeX[square];
            case 1:
                return -shapeY[square];
            case 2:
                return -shapeX[square];
            case 3:
                return shapeY[square];
            default:
                return 0; // 발생하지 않아야 함
        }
    }

    private int getRelativeY(int square, int orientation) {
        switch (orientation % 4) {
            case 0:
                return shapeY[square];
            case 1:
                return shapeX[square];
            case 2:
                return -shapeY[square];
            case 3:
                return -shapeX[square];
            default:
                return 0; // 발생하지 않아야 함
        }
    }

    private void paint(Color color) {
        int x, y;

        for (int i = 0; i < shapeX.length; i++) {
            x = xPos + getRelativeX(i, orientation);
            y = yPos + getRelativeY(i, orientation);
            board.setSquareColor(x, y, color);
        }
    }
}