package Test;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * 테트리스 게임의 메인 클래스입니다.
 */
public class Tetris {
    private GameEx game = null;
    private String playerName = ""; // 플레이어 이름 저장
    private HighScoreBoard highScoreBoard; // 점수판 추가
    private boolean gameOverInProgress = false;

    public static void main(String[] args) {
        new Tetris().startGame(); // Tetris 인스턴스를 생성하고 startGame 메서드 호출
    }

    public void startGame() {
        Frame frame = new Frame("Tetris");
        game = new GameEx(); // Game 인스턴스 초기화
        highScoreBoard = new HighScoreBoard(); // HighScoreBoard 초기화

        final TextArea taHiScores = new TextArea("", 10, 30, TextArea.SCROLLBARS_NONE); // 너비 조정
        taHiScores.setBackground(Color.black);
        taHiScores.setForeground(Color.white);
        taHiScores.setFont(new Font("monospaced", Font.PLAIN , 11));
        taHiScores.setEditable(false);

     // 텍스트 길이에 맞춰 공백을 추가하여 가운데 정렬
        StringBuilder sb = new StringBuilder();
        sb.append("         득점자 점수판                  \n"); // 앞에 공백 추가
        sb.append(" ------------------------------\n");
        sb.append("    RANK     PLAYER    SCORE    \n");
        taHiScores.setText(sb.toString());

        // 새로운 패널 추가
        Panel noticePanel = new Panel();
        noticePanel.setLayout(new GridLayout(4, 1)); // 4행 1열로 설정

        JLabel nicknameNotice = new JLabel("* 게임 시작 : 아래 play 버튼을 누르시오.", JLabel.CENTER);
        nicknameNotice.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));

        JLabel startNotice = new JLabel("(닉네임을 입력해야 게임을 시작하실 수 있습니다.)", JLabel.CENTER);
        startNotice.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));

        JLabel pauseNotice = new JLabel("* 일시 정지 : Enter 키", JLabel.CENTER);
        pauseNotice.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));

        // 키 조작 방법 추가
        JLabel controlNotice = new JLabel("* KEY 조작 방법 : 방향키 + Space Bar", JLabel.CENTER);
        controlNotice.setFont(new Font("Malgun Gothic", Font.PLAIN, 15));

        // 패널에 레이블 추가
        noticePanel.add(nicknameNotice);
        noticePanel.add(startNotice);
        noticePanel.add(pauseNotice);
        noticePanel.add(controlNotice); // 키 조작 방법 추가

        game.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("state")) {
                    int state = ((Integer) evt.getNewValue()).intValue();
                    if (state == GameEx.STATE_GAMEOVER && !gameOverInProgress) {
                        gameOverInProgress = true; // 플래그를 true로 설정
                        int score = game.getScore(); // 점수 가져오기
                        highScoreBoard.addScore(playerName, score); // 점수판에 추가
                        taHiScores.setText(highScoreBoard.getScores()); // 점수판 업데이트
                        askForNewNickname(frame); // 닉네임 입력 요청
                    }
                }
            }
        });

        Button btnStart = new Button("Play");
        btnStart.setFocusable(false);
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                askForNewNickname(frame); // 닉네임 입력 요청
            }
        });

        final Container c = new Container();
        c.setLayout(new BorderLayout());
        c.add(game.getSquareBoardComponent(), BorderLayout.CENTER);
        c.add(btnStart, BorderLayout.SOUTH);

        final Container c2 = new Container();
        c2.setLayout(new GridLayout(1, 2));
        c2.add(c);
        c2.add(taHiScores);

        frame.add(c2, BorderLayout.CENTER);
        frame.add(noticePanel, BorderLayout.SOUTH); // 공지문 패널 추가

        frame.pack();

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }
    
    private void askForNewNickname(Frame frame) {
        String inputName = JOptionPane.showInputDialog(frame, "닉네임을 입력하세요(5자 이내):");
        if (inputName != null && !inputName.trim().isEmpty()) {
            if (inputName.length() > 5) { // 5자 초과 체크
                JOptionPane.showMessageDialog(frame, "닉네임은 5자 이내로 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
                askForNewNickname(frame); // 재입력 요청
                return;
                }
            playerName = inputName; // 입력된 닉네임 저장
            gameOverInProgress = false; // 플래그 리셋
            game.init(); // 게임 초기화
            game.start(); // 게임 시작
        } else {
            // 닉네임이 비어있을 경우 경고 다이얼로그
            JOptionPane.showMessageDialog(frame, "닉네임을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
        }
    }
}
