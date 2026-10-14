import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/** Draws the currently selected tree structure. */
public class TreePanel extends JPanel {
    private static final int AVL_DIAMETER = 54;
    private static final int AVL_GAP = 92;
    private static final int TOP_MARGIN = 60;
    private static final int LEVEL_GAP = 112;
    private static final int B_KEY_WIDTH = 52;
    private static final int B_KEY_HEIGHT = 38;
    private static final int B_KEY_SPACING = 60;
    //private static final int B_NODE_GAP = 2;
    private static final int B_LEFT_MARGIN = 70;

    private String treeType = "AVL Tree";
    private AVLTree.Node avlRoot;
    private BTree.Node bTreeRoot;
    private BPlusTree.Node bPlusRoot;
    private Integer highlightedKey;
    private final Set<Integer> highlightedKeys = new HashSet<>();
    private final Map<AVLTree.Node, Point> avlPositions = new HashMap<>();
    private final Map<BTree.Node, BNodePosition> bPositions = new HashMap<>();
    private final Map<BPlusTree.Node, BPlusNodePosition> bPlusPositions = new HashMap<>();

    private static class BNodePosition {
        int[] keyCenters;
        int centerX;
        int y;
        BNodePosition(int[] keyCenters, int y) {
            this.keyCenters = keyCenters;
            this.y = y;
            this.centerX = (keyCenters[0] + keyCenters[keyCenters.length - 1]) / 2;
        }
    }

    private static class BPlusNodePosition {
        int[] keyCenters;
        int centerX;
        int y;
        int subtreeLeft;
        int subtreeRight;
        BPlusNodePosition(int[] keyCenters, int centerX, int y, int subtreeLeft, int subtreeRight) {
            this.keyCenters = keyCenters;
            this.centerX = centerX;
            this.y = y;
            this.subtreeLeft = subtreeLeft;
            this.subtreeRight = subtreeRight;
        }
    }

    public TreePanel() {
        setBackground(new Color(245, 247, 250));
        setOpaque(true);
        updatePreferredSize();
    }

    public void setTreeType(String treeType) {
        this.treeType = treeType;
        updatePreferredSize();
        revalidate();
        repaint();
    }

    public void setAvlRoot(AVLTree.Node root) {
        this.avlRoot = root;
        updatePreferredSize();
        revalidate();
        repaint();
    }

    public void setBTreeRoot(BTree.Node root) {
        this.bTreeRoot = root;
        updatePreferredSize();
        revalidate();
        repaint();
    }

    public void setBPlusRoot(BPlusTree.Node root) {
        this.bPlusRoot = root;
        updatePreferredSize();
        revalidate();
        repaint();
    }

    public void setHighlightedKey(Integer key) {
        this.highlightedKey = key;
        this.highlightedKeys.clear(); //clear the hashset
        repaint();
    }

    public void setHighlightedKeys(Set<Integer> keys) {
        this.highlightedKey = null;
        this.highlightedKeys.clear();
        if (keys != null) this.highlightedKeys.addAll(keys);
        repaint();
    }

    private boolean isHighlighted(AVLTree.Node key) {
        return (highlightedKey != null && highlightedKey == key.key) || highlightedKeys.contains(key.key);
    }

    public void clearTree() {
        avlRoot = null;
        bTreeRoot = null;
        bPlusRoot = null;
        highlightedKey = null;
        highlightedKeys.clear();
        updatePreferredSize();
        revalidate();
        repaint();
    }

    private void updatePreferredSize() {
        int width;
        int depth;
        if ("AVL Tree".equals(treeType)) {
            width = Math.max(700, 110 + avlCount(avlRoot) * AVL_GAP);
            depth = avlDepth(avlRoot);
        } else if ("B-Tree".equals(treeType)) {
            width = Math.max(700, 2 * B_LEFT_MARGIN + Math.max(1, bTreeSize(bTreeRoot)) * B_KEY_SPACING);
            depth = bTreeDepth(bTreeRoot);
        } else {
            width = Math.max(700, 2 * B_LEFT_MARGIN
                    + (bPlusSize(bPlusRoot) + bPlusLeafCount(bPlusRoot) + 2) * B_KEY_SPACING);
            depth = bPlusDepth(bPlusRoot);
        }
        int height = Math.max(480, TOP_MARGIN + Math.max(1, depth) * LEVEL_GAP + 100);
        setPreferredSize(new Dimension(width, height));
    }

    private int avlCount(AVLTree.Node node) {
        return node == null ? 0 : 1 + avlCount(node.left) + avlCount(node.right);
    }

    private int avlDepth(AVLTree.Node node) {
        return node == null ? 0 : 1 + Math.max(avlDepth(node.left), avlDepth(node.right));
    }

    private int bTreeSize(BTree.Node node) {
        if (node == null) return 0;
        int total = node.keys.size();
        for (BTree.Node child : node.children) total += bTreeSize(child);
        return total;
    }

    private int bTreeDepth(BTree.Node node) {
        if (node == null) return 0;
        if (node.leaf) return 1;
        return 1 + bTreeDepth(node.children.get(0));
    }

    private int bPlusSize(BPlusTree.Node node) {
        if (node == null) return 0;
        if (node.leaf) return node.keys.size();
        int total = 0;
        for (BPlusTree.Node child : node.children) total += bPlusSize(child);
        return total;
    }

    private int bPlusLeafCount(BPlusTree.Node node) {
        if (node == null) return 0;
        if (node.leaf) return 1;
        int total = 0;
        for (BPlusTree.Node child : node.children) total += bPlusLeafCount(child);
        return total;
    }

    private int bPlusDepth(BPlusTree.Node node) {
        if (node == null) return 0;
        if (node.leaf) return 1;
        return 1 + bPlusDepth(node.children.get(0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if ("AVL Tree".equals(treeType)) {
                drawAvlTree(g2);
            } else if ("B-Tree".equals(treeType)) {
                drawBTree(g2);
            } else {
                drawBPlusTree(g2);
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawPlaceholder(Graphics2D g2, String title, String subtitle) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.setColor(new Color(100, 110, 125));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, Math.max(20, (getWidth() - fm.stringWidth(title)) / 2), getHeight() / 2 - 8);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, Math.max(20, (getWidth() - fm.stringWidth(subtitle)) / 2), getHeight() / 2 + 22);
    }

    // ---------------- AVL rendering ----------------
    private void drawAvlTree(Graphics2D g2) {
        if (avlRoot == null) {
            drawPlaceholder(g2, "Your AVL tree will appear here", "Enter an integer and click Insert to begin.");
            return;
        }
        avlPositions.clear();
        List<AVLTree.Node> inOrder = new ArrayList<>();
        collectAvlInOrder(avlRoot, inOrder); //puts all the nodes in the list in inorder mode
        Map<AVLTree.Node, Integer> orderIndex = new HashMap<>();
        for (int i = 0; i < inOrder.size(); i++) orderIndex.put(inOrder.get(i), i);
        assignAvlPositions(avlRoot, 0, orderIndex, inOrder.size());
        g2.setStroke(new BasicStroke(2.0f));
        g2.setColor(new Color(151, 164, 184));
        drawAvlEdges(g2, avlRoot);
        drawAvlNodes(g2, avlRoot);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(95, 105, 120));
        g2.drawString("Node labels: value | h = height | BF = balance factor", 16, getHeight() - 18);
    }

    private void collectAvlInOrder(AVLTree.Node node, List<AVLTree.Node> nodes) {
        if (node == null) return;
        collectAvlInOrder(node.left, nodes);
        nodes.add(node);
        collectAvlInOrder(node.right, nodes);
    }

    private void assignAvlPositions(AVLTree.Node node, int depth, Map<AVLTree.Node, Integer> orderIndex, int total) {
        if (node == null) return;
        int index = orderIndex.get(node);
        int x = 50 + index * AVL_GAP + AVL_GAP / 2;
        int y = TOP_MARGIN + depth * LEVEL_GAP;
        avlPositions.put(node, new Point(x, y));
        assignAvlPositions(node.left, depth + 1, orderIndex, total);
        assignAvlPositions(node.right, depth + 1, orderIndex, total);
    }

    private void drawAvlEdges(Graphics2D g2, AVLTree.Node node) {
        if (node == null) return;
        Point parent = avlPositions.get(node);
        if (node.left != null) {
            Point child = avlPositions.get(node.left);
            g2.drawLine(parent.x, parent.y + AVL_DIAMETER / 2 - 2, child.x, child.y - AVL_DIAMETER / 2 + 2);
            drawAvlEdges(g2, node.left);
        }
        if (node.right != null) {
            Point child = avlPositions.get(node.right);
            g2.drawLine(parent.x, parent.y + AVL_DIAMETER / 2 - 2, child.x, child.y - AVL_DIAMETER / 2 + 2);
            drawAvlEdges(g2, node.right);
        }
    }

    private void drawAvlNodes(Graphics2D g2, AVLTree.Node node) {
        if (node == null) return;
        Point p = avlPositions.get(node);
        int left = p.x - AVL_DIAMETER / 2;
        int top = p.y - AVL_DIAMETER / 2;
        boolean searchedNode = isHighlighted(node);
        g2.setColor(node.highlightedNodes==1 ? (searchedNode ?  new Color(255, 222, 140) : new Color(255, 127, 127)) : new Color(219, 234, 254));
        //g2.setColor(searchedNode ? new Color(255, 222, 140) : new Color(219, 234, 254)); //infill color of oval
        g2.fillOval(left, top, AVL_DIAMETER, AVL_DIAMETER);
        g2.setStroke(new BasicStroke(searchedNode ? 3.0f : 2.0f));
        g2.setColor(searchedNode ? new Color(180, 105, 20) : new Color(59, 130, 246)); //outline color of oval
        g2.drawOval(left, top, AVL_DIAMETER, AVL_DIAMETER);
        String value = Integer.toString(node.key);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(25, 45, 85));
        g2.drawString(value, p.x - fm.stringWidth(value) / 2, p.y + fm.getAscent() / 2 - 2);
        int bf = height(node.left) - height(node.right);
        String info = "h=" + node.height + "  BF=" + bf;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        fm = g2.getFontMetrics();
        g2.setColor(new Color(75, 85, 100));
        g2.drawString(info, p.x - fm.stringWidth(info) / 2, top + AVL_DIAMETER + 16);
        drawAvlNodes(g2, node.left);
        drawAvlNodes(g2, node.right);
    }

    private int height(AVLTree.Node node) {
        return node == null ? 0 : node.height;
    }

    // ---------------- B-Tree rendering ----------------
    private void drawBTree(Graphics2D g2) {
        if (bTreeRoot == null) {
            drawPlaceholder(g2, "Your B-Tree will appear here", "Order 4 B-Tree: each node can contain up to 3 keys.");
            return;
        }
        bPositions.clear();
        int[] cursor = {0};
        assignBPositions(bTreeRoot, 0, cursor);
        g2.setStroke(new BasicStroke(2.0f));
        g2.setColor(new Color(151, 164, 184));
        drawBEdges(g2, bTreeRoot);
        drawBNodes(g2, bTreeRoot);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(95, 105, 120));
        g2.drawString("B-Tree order 4 | Each rectangle is a key slot | highlighted key shown in amber", 16, getHeight() - 18);
    }

    private void assignBPositions(BTree.Node node, int depth, int[] cursor) {
        if (node == null) return;
        int[] centers = new int[node.keys.size()];
        for (int i = 0; i < node.keys.size(); i++) {
            if (!node.leaf) assignBPositions(node.children.get(i), depth + 1, cursor);
            centers[i] = B_LEFT_MARGIN + cursor[0] * B_KEY_SPACING;
            cursor[0]++;
        }
        if (!node.leaf) assignBPositions(node.children.get(node.keys.size()), depth + 1, cursor);
        bPositions.put(node, new BNodePosition(centers, TOP_MARGIN + depth * LEVEL_GAP));
    }

    private void drawBEdges(Graphics2D g2, BTree.Node node) {
        if (node == null || node.leaf) return;
        BNodePosition parent = bPositions.get(node);
        for (int i = 0; i < node.children.size(); i++) {
            BTree.Node childNode = node.children.get(i);
            BNodePosition child = bPositions.get(childNode);
            int anchorX;
            if (i == 0) anchorX = parent.keyCenters[0] - B_KEY_WIDTH / 2;
            else if (i == node.keys.size()) anchorX = parent.keyCenters[i - 1] + B_KEY_WIDTH / 2;
            else anchorX = (parent.keyCenters[i - 1] + parent.keyCenters[i]) / 2;
            g2.drawLine(anchorX, parent.y + B_KEY_HEIGHT / 2, child.centerX, child.y - B_KEY_HEIGHT / 2);
            drawBEdges(g2, childNode);
        }
    }

    private void drawBNodes(Graphics2D g2, BTree.Node node) {
        if (node == null) return;
        BNodePosition p = bPositions.get(node);
        for (int i = 0; i < node.keys.size(); i++) {
            int key = node.keys.get(i);
            int x = p.keyCenters[i] - B_KEY_WIDTH / 2;
            int y = p.y - B_KEY_HEIGHT / 2;
            boolean highlighted = isHighlighted(key);
            g2.setColor(highlighted ? new Color(255, 222, 140) : new Color(219, 234, 254));
            g2.fillRoundRect(x, y, B_KEY_WIDTH, B_KEY_HEIGHT, 8, 8);
            g2.setStroke(new BasicStroke(highlighted ? 3.0f : 2.0f));
            g2.setColor(highlighted ? new Color(180, 105, 20) : new Color(59, 130, 246));
            g2.drawRoundRect(x, y, B_KEY_WIDTH, B_KEY_HEIGHT, 8, 8);
            String value = Integer.toString(key);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(25, 45, 85));
            g2.drawString(value, p.keyCenters[i] - fm.stringWidth(value) / 2, p.y + fm.getAscent() / 2 - 2);
        }
        for (BTree.Node child : node.children) drawBNodes(g2, child);
    }

    // ---------------- B+ Tree rendering ----------------
    private void drawBPlusTree(Graphics2D g2) {
        if (bPlusRoot == null) {
            drawPlaceholder(g2, "Your B+ Tree will appear here",
                    "Insert values to build an order 4 B+ Tree with linked leaf nodes.");
            return;
        }
        bPlusPositions.clear();
        int[] cursor = {0};
        assignBPlusPositions(bPlusRoot, 0, cursor);

        g2.setStroke(new BasicStroke(1.8f));
        g2.setColor(new Color(151, 164, 184));
        drawBPlusEdges(g2, bPlusRoot);
        drawLeafLinks(g2);
        drawBPlusNodes(g2, bPlusRoot);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(65, 105, 95));
        g2.drawString("B+ Tree order 4 | Blue = index separators | Green = leaf data | Dashed arrows link leaves",
                16, getHeight() - 18);
    }

    private BPlusNodePosition assignBPlusPositions(BPlusTree.Node node, int depth, int[] cursor) {
        int y = TOP_MARGIN + depth * LEVEL_GAP;
        if (node.leaf) {
            int[] centers = new int[node.keys.size()];
            for (int i = 0; i < centers.length; i++) {
                centers[i] = B_LEFT_MARGIN + cursor[0] * B_KEY_SPACING;
                cursor[0]++;
            }
            // Leave a visible gap between linked leaf nodes.
            cursor[0]++;
            int left = centers[0] - B_KEY_WIDTH / 2;
            int right = centers[centers.length - 1] + B_KEY_WIDTH / 2;
            int center = (centers[0] + centers[centers.length - 1]) / 2;
            BPlusNodePosition pos = new BPlusNodePosition(centers, center, y, left, right);
            bPlusPositions.put(node, pos);
            return pos;
        }

        List<BPlusNodePosition> childPositions = new ArrayList<>();
        for (BPlusTree.Node child : node.children) {
            childPositions.add(assignBPlusPositions(child, depth + 1, cursor));
        }
        BPlusNodePosition first = childPositions.get(0);
        BPlusNodePosition last = childPositions.get(childPositions.size() - 1);
        int center = (first.subtreeLeft + last.subtreeRight) / 2;
        int[] centers = new int[node.keys.size()];
        int start = center - ((centers.length - 1) * B_KEY_SPACING) / 2;
        for (int i = 0; i < centers.length; i++) centers[i] = start + i * B_KEY_SPACING;
        BPlusNodePosition pos = new BPlusNodePosition(
                centers, center, y, first.subtreeLeft, last.subtreeRight);
        bPlusPositions.put(node, pos);
        return pos;
    }

    private void drawBPlusEdges(Graphics2D g2, BPlusTree.Node node) {
        if (node == null || node.leaf) return;
        BPlusNodePosition parent = bPlusPositions.get(node);
        for (int i = 0; i < node.children.size(); i++) {
            BPlusTree.Node childNode = node.children.get(i);
            BPlusNodePosition child = bPlusPositions.get(childNode);
            int anchorX;
            if (i == 0) anchorX = parent.keyCenters[0] - B_KEY_SPACING / 2;
            else if (i == node.keys.size()) anchorX = parent.keyCenters[i - 1] + B_KEY_SPACING / 2;
            else anchorX = (parent.keyCenters[i - 1] + parent.keyCenters[i]) / 2;
            g2.drawLine(anchorX, parent.y + B_KEY_HEIGHT / 2,
                    child.centerX, child.y - B_KEY_HEIGHT / 2);
            drawBPlusEdges(g2, childNode);
        }
    }

    private void drawLeafLinks(Graphics2D g2) {
        List<BPlusTree.Node> leaves = new ArrayList<>();
        collectBPlusLeaves(bPlusRoot, leaves);
        StrokeWithColor old = new StrokeWithColor(g2.getStroke(), g2.getColor());
        g2.setColor(new Color(13, 148, 136));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10.0f, new float[]{6.0f, 5.0f}, 0.0f));
        for (int i = 0; i + 1 < leaves.size(); i++) {
            BPlusNodePosition left = bPlusPositions.get(leaves.get(i));
            BPlusNodePosition right = bPlusPositions.get(leaves.get(i + 1));
            int y = left.y + B_KEY_HEIGHT / 2 + 11;
            int x1 = left.subtreeRight - 1;
            int x2 = right.subtreeLeft + 1;
            g2.drawLine(x1, y, x2, y);
            // Arrowhead points to the next leaf.
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawLine(x2, y, x2 - 7, y - 4);
            g2.drawLine(x2, y, x2 - 7, y + 4);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    10.0f, new float[]{6.0f, 5.0f}, 0.0f));
        }
        g2.setStroke(old.stroke);
        g2.setColor(old.color);
    }

    private static class StrokeWithColor {
        final java.awt.Stroke stroke;
        final Color color;
        StrokeWithColor(java.awt.Stroke stroke, Color color) {
            this.stroke = stroke;
            this.color = color;
        }
    }

    private void collectBPlusLeaves(BPlusTree.Node node, List<BPlusTree.Node> leaves) {
        if (node == null) return;
        if (node.leaf) {
            leaves.add(node);
            return;
        }
        for (BPlusTree.Node child : node.children) collectBPlusLeaves(child, leaves);
    }

    private void drawBPlusNodes(Graphics2D g2, BPlusTree.Node node) {
        if (node == null) return;
        BPlusNodePosition pos = bPlusPositions.get(node);
        int y = pos.y - B_KEY_HEIGHT / 2;
        Color normalFill = node.leaf ? new Color(209, 250, 229) : new Color(219, 234, 254);
        Color normalBorder = node.leaf ? new Color(13, 148, 136) : new Color(59, 130, 246);

        for (int i = 0; i < node.keys.size(); i++) {
            int key = node.keys.get(i);
            int x = pos.keyCenters[i] - B_KEY_WIDTH / 2;
            boolean highlighted = isHighlighted(key);
            g2.setColor(highlighted ? new Color(255, 222, 140) : normalFill);
            g2.fillRoundRect(x, y, B_KEY_WIDTH, B_KEY_HEIGHT, 8, 8);
            g2.setStroke(new BasicStroke(highlighted ? 3.0f : 2.0f));
            g2.setColor(highlighted ? new Color(180, 105, 20) : normalBorder);
            g2.drawRoundRect(x, y, B_KEY_WIDTH, B_KEY_HEIGHT, 8, 8);
            String value = Integer.toString(key);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(25, 45, 85));
            g2.drawString(value, pos.keyCenters[i] - fm.stringWidth(value) / 2,
                    pos.y + fm.getAscent() / 2 - 2);
        }

        String typeLabel = node.leaf ? "LEAF" : "INDEX";
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        FontMetrics labelMetrics = g2.getFontMetrics();
        g2.setColor(node.leaf ? new Color(13, 115, 99) : new Color(45, 85, 145));
        g2.drawString(typeLabel, pos.centerX - labelMetrics.stringWidth(typeLabel) / 2,
                y - 5);

        for (BPlusTree.Node child : node.children) drawBPlusNodes(g2, child);
    }

}
