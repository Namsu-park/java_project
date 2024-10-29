package Test;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class SquareBoard extends Object {

    private final int width;

    private final int height;

    private Color[][] matrix = null;

    private String message = null;

    private int removedLines = 0;

    private final SquareBoardComponent component;

    public SquareBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.matrix = new Color[height][width];
        this.component = new SquareBoardComponent();
        clear();
    }

    public boolean isSquareEmpty(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return x >= 0 && x < width && y < 0;
        } else {
            return matrix[y][x] == null;
        }
    }

    public boolean isLineEmpty(int y) {
        if (y < 0 || y >= height) {
            return false;
        }
        for (int x = 0; x < width; x++) {
            if (matrix[y][x] != null) {
                return false;
            }
        }
        return true;
    }

    public boolean isLineFull(int y) {
        if (y < 0 || y >= height) {
            return true;
        }
        for (int x = 0; x < width; x++) {
            if (matrix[y][x] == null) {
                return false;
            }
        }
        return true;
    }

    public boolean hasFullLines() {
        for (int y = height - 1; y >= 0; y--) {
            if (isLineFull(y)) {
                return true;
            }
        }
        return false;
    }

    public Component getComponent() {
        return component;
    }

    public int getBoardHeight() {
        return height;
    }

    public int getBoardWidth() {
        return width;
    }

    public int getRemovedLines() {
        return removedLines;
    }

    public Color getSquareColor(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        } else {
            return matrix[y][x];
        }
    }

    public void setSquareColor(int x, int y, Color color) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return;
        }
        matrix[y][x] = color;
        if (component != null) {
            component.invalidateSquare(x, y);
        }
    }

    public void setMessage(String message) {
        this.message = message;
        if (component != null) {
            component.redrawAll();
        }
    }

    public void clear() {
        removedLines = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.matrix[y][x] = null;
            }
        }
        if (component != null) {
            component.redrawAll();
        }
    }

    public void removeFullLines() {
        boolean repaint = false;

        for (int y = height - 1; y >= 0; y--) {
            if (isLineFull(y)) {
                removeLine(y);
                removedLines++;
                repaint = true;
                y++;
            }
        }

        if (repaint && component != null) {
            component.redrawAll();
        }
    }

    private void removeLine(int y) {
        if (y < 0 || y >= height) {
            return;
        }
        for (; y > 0; y--) {
            for (int x = 0; x < width; x++) {
                matrix[y][x] = matrix[y - 1][x];
            }
        }
        for (int x = 0; x < width; x++) {
            matrix[0][x] = null;
        }
    }

    public void update() {
        component.redraw();
    }

    private class SquareBoardComponent extends JComponent {

        /**
         * The component size. If the component has been resized, that
         * will be detected when the paint method executes. If this
         * value is set to null, the component dimensions are unknown.
         */
        private Dimension size = null;

        private Insets insets = new Insets(0, 0, 0, 0);

        private Dimension squareSize = new Dimension(0, 0);

        private Image bufferImage = null;

        private Rectangle bufferRect = new Rectangle();

        private Color messageColor = Color.white;

        private Hashtable lighterColors = new Hashtable();

        private Hashtable darkerColors = new Hashtable();

        private boolean updated = true;

        private Rectangle updateRect = new Rectangle();

        public SquareBoardComponent() {
            setBackground(Configuration.getColor("board.background",
                    "#000000"));
            messageColor = Configuration.getColor("board.message",
                    "#ffffff");
        }

        public void invalidateSquare(int x, int y) {
            if (updated) {
                updated = false;
                updateRect.x = x;
                updateRect.y = y;
                updateRect.width = 0;
                updateRect.height = 0;
            } else {
                if (x < updateRect.x) {
                    updateRect.width += updateRect.x - x;
                    updateRect.x = x;
                } else if (x > updateRect.x + updateRect.width) {
                    updateRect.width = x - updateRect.x;


                }
                if (y < updateRect.y) {
                    updateRect.height += updateRect.y - y;
                    updateRect.y = y;
                } else if (y > updateRect.y + updateRect.height) {
                    updateRect.height = y - updateRect.y;
                }
            }
        }


        public void redraw() {
            Graphics g;

            if (!updated) {
                updated = true;
                g = getGraphics();
                if (g == null) return;
                g.setClip(insets.left + updateRect.x * squareSize.width,
                        insets.top + updateRect.y * squareSize.height,
                        (updateRect.width + 1) * squareSize.width,
                        (updateRect.height + 1) * squareSize.height);
                paint(g);
            }
        }


        public void redrawAll() {
            Graphics g;

            updated = true;
            g = getGraphics();
            if (g == null) return;
            g.setClip(insets.left,
                    insets.top,
                    width * squareSize.width,
                    height * squareSize.height);
            paint(g);
        }

        public boolean isDoubleBuffered() {
            return true;
        }

        public Dimension getPreferredSize() {
            return new Dimension(width * 20, height * 20);
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }


        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        private Color getLighterColor(Color c) {
            Color lighter;

            lighter = (Color) lighterColors.get(c);
            if (lighter == null) {
                lighter = c.brighter().brighter();
                lighterColors.put(c, lighter);
            }
            return lighter;
        }

        private Color getDarkerColor(Color c) {
            Color darker;

            darker = (Color) darkerColors.get(c);
            if (darker == null) {
                darker = c.darker().darker();
                darkerColors.put(c, darker);
            }
            return darker;
        }

        public synchronized void paint(Graphics g) {
            Graphics bufferGraphics;
            Rectangle rect;

            if (size == null || !size.equals(getSize())) {
                size = getSize();
                squareSize.width = size.width / width;
                squareSize.height = size.height / height;

                //if (squareSize.width <= squareSize.height) {
                //} else {
                //}

                insets.left = (size.width - width * squareSize.width) / 2;
                insets.right = insets.left;
                insets.top = 0;
                insets.bottom = size.height - height * squareSize.height;
                bufferImage = createImage(width * squareSize.width,
                        height * squareSize.height);
            }

            rect = g.getClipBounds();
            bufferGraphics = bufferImage.getGraphics();
            bufferGraphics.setClip(rect.x - insets.left,
                    rect.y - insets.top,
                    rect.width,
                    rect.height);
            doPaintComponent(bufferGraphics);

            g.drawImage(bufferImage,
                    insets.left,
                    insets.top,
                    getBackground(),
                    null);
        }

        private void doPaintComponent(Graphics g) {

            g.setColor(getBackground());
            g.fillRect(0,
                    0,
                    width * squareSize.width,
                    height * squareSize.height);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix[y][x] != null) {
                        paintSquare(g, x, y);
                    }
                }
            }

            if (message != null) {
                paintMessage(g, message);
            }
        }

        private void paintSquare(Graphics g, int x, int y) {
            Color color = matrix[y][x];
            int xMin = x * squareSize.width;
            int yMin = y * squareSize.height;
            int xMax = xMin + squareSize.width - 1;
            int yMax = yMin + squareSize.height - 1;
            int i;

            bufferRect.x = xMin;
            bufferRect.y = yMin;
            bufferRect.width = squareSize.width;
            bufferRect.height = squareSize.height;
            if (!bufferRect.intersects(g.getClipBounds())) {
                return;
            }

            g.setColor(color);
            g.fillRect(xMin, yMin, squareSize.width, squareSize.height);

            g.setColor(getLighterColor(color));
            for (i = 0; i < squareSize.width / 10; i++) {
                g.drawLine(xMin + i, yMin + i, xMax - i, yMin + i);


                g.drawLine(xMin + i, yMin + i, xMin + i, yMax - i);
            }

            g.setColor(getDarkerColor(color));
            for (i = 0; i < squareSize.width / 10; i++) {
                g.drawLine(xMax - i, yMin + i, xMax - i, yMax - i);
                g.drawLine(xMin + i, yMax - i, xMax - i, yMax - i);
            }
        }

        private void paintMessage(Graphics g, String msg) {
            int fontWidth;
            int offset;
            int x;
            int y;

            g.setFont(new Font("SansSerif", Font.BOLD, squareSize.width - 2));
            fontWidth = g.getFontMetrics().stringWidth(msg);

            x = (width * squareSize.width - fontWidth) / 2;
            y = height * squareSize.height / 2;

            offset = squareSize.width / 10;
            g.setColor(Color.black);
            g.drawString(msg, x - offset, y - offset);
            g.drawString(msg, x - offset, y);
            g.drawString(msg, x - offset, y - offset);
            g.drawString(msg, x, y - offset);
            g.drawString(msg, x, y + offset);
            g.drawString(msg, x + offset, y - offset);
            g.drawString(msg, x + offset, y);
            g.drawString(msg, x + offset, y + offset);

            g.setColor(messageColor);
            g.drawString(msg, x, y);
        }
    }
}