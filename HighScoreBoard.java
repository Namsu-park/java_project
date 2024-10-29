package Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class HighScore {
    String playerName;
    int score;

    public HighScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
    }
}

public class HighScoreBoard {
    private final ArrayList<HighScore> scores;

    public HighScoreBoard() {
        scores = new ArrayList<>();
    }

    public void addScore(String playerName, int score) {
        boolean found = false;
        for (HighScore highScore : scores) {
            if (highScore.playerName.equals(playerName)) {
                if (score > highScore.score) {
                    highScore.score = score; // 점수 업데이트
                }
                found = true;
                break;
            }
        }
        if (!found) {
            scores.add(new HighScore(playerName, score)); // 새로운 점수 추가
        }
        
        // 점수 리스트 정렬
        Collections.sort(scores, Comparator.comparingInt(h -> -h.score));
    }
    
    public String centerAlign(String text, int width) {
        int padding = (width - text.length()) / 2;
        if (padding < 0) padding = 0; // 패딩이 음수가 되지 않도록 조정
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            sb.append(" "); // 좌측 패딩 추가
        }
        sb.append(text);
        for (int i = 0; i < padding; i++) {
            sb.append(" "); // 우측 패딩 추가
        }
        return sb.toString();
    }

    public String getScores() {
        StringBuilder sb = new StringBuilder();
        sb.append("       득점자 점수판                  \n");
        sb.append(" ---------------------------\n");
        sb.append(centerAlign("RANK", 10));
        sb.append(centerAlign("PLAYER", 10));
        sb.append(centerAlign("SCORE", 10));
        sb.append("\n ---------------------------\n");
        
        for (int i = 0; i < scores.size(); i++) {
            HighScore highScore = scores.get(i);
            sb.append(centerAlign(String.valueOf(i + 1), 10));
            sb.append(centerAlign(highScore.playerName, 10));
            
            // 점수의 길이에 따라 가운데 정렬
            String scoreStr = String.valueOf(highScore.score);
            int scoreWidth = 7; // 점수의 고정 너비
            sb.append(centerAlign(scoreStr, scoreWidth));
            
            sb.append("\n");
        }
        return sb.toString();
    }
}
