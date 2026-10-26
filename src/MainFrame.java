import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainFrame extends JFrame {
    private final JComboBox<String> treeSelector;
    private final JTextField valueField;
    private final JLabel statusLabel;
    private final JLabel statsLabel;
    private final TreePanel treePanel;
    private final AVLTree avlTree;
    private final BTree bTree;
    private final BPlusTree bPlusTree;
    private final JButton insertButton;
    private final JButton searchButton;
    private final JButton rangeSearchButton;
    private final JButton deleteButton;
    private final JButton clearButton;
    
    String helpTextString="<html><div style='width:180px'>AVL Tree:self-balancing binary search tree.<br><br>B-Tree: order 4; nodes can contain up to 3 sorted keys.<br><br>B+ Tree: data is stored in linked leaves; internal keys guide the search. Use Range Search to query an interval.</div></html>";

    public MainFrame() {
        avlTree = new AVLTree();
        bTree = new BTree();
        bPlusTree = new BPlusTree();
        setTitle("Tree Data Structure Visualizer");
        setSize(1100, 740);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        setIconImage(new ImageIcon(Main.class.getResource("logo.png")).getImage());

        JLabel title = new JLabel("Tree Data Structure Visualizer", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setOpaque(true);
        title.setBackground(new Color(25, 45, 85));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
        add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(Color.WHITE);
        controls.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 225, 232)),
                BorderFactory.createEmptyBorder(18, 15, 18, 15)));
        controls.setPreferredSize(new Dimension(235, 0));

        JLabel treeLabel = new JLabel("Select Tree Type");
        treeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        treeSelector = new JComboBox<>(new String[]{"AVL Tree", "B-Tree", "B+ Tree"});
        treeSelector.setMaximumSize(new Dimension(205, 36));
        treeSelector.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel("Enter Integer Value");
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        valueField = new JTextField();
        valueField.setMaximumSize(new Dimension(205, 36));
        valueField.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueField.setToolTipText("Enter an integer, then click an operation");

        insertButton = makeButton("Insert", new Color(26, 54, 93));
        searchButton = makeButton("Search", new Color(27, 153, 139));
        this.rangeSearchButton = new JButton();
        //rangeSearchButton = makeButton("Range Search (B+)", new Color(124, 58, 237));
        deleteButton = makeButton("Delete", new Color(217, 83, 79));
        clearButton = makeButton("Clear Tree", new Color(74, 85, 104));

        controls.add(treeLabel);
        controls.add(Box.createVerticalStrut(8));
        controls.add(treeSelector);
        controls.add(Box.createVerticalStrut(22));
        controls.add(valueLabel);
        controls.add(Box.createVerticalStrut(8));
        controls.add(valueField);
        controls.add(Box.createVerticalStrut(18));
        controls.add(insertButton);
        controls.add(Box.createVerticalStrut(9));
        controls.add(searchButton);
        controls.add(Box.createVerticalStrut(9));
        //controls.add(rangeSearchButton);
        controls.add(Box.createVerticalStrut(9));
        controls.add(deleteButton);
        controls.add(Box.createVerticalStrut(9));
        controls.add(clearButton);
        controls.add(Box.createVerticalStrut(24));

        JLabel helpTitle = new JLabel("About This Mode");
        helpTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel helpText = new JLabel(helpTextString);
        helpText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        helpText.setForeground(new Color(75, 85, 100));
        controls.add(helpTitle);
        controls.add(Box.createVerticalStrut(8));
        controls.add(helpText);
        add(controls, BorderLayout.WEST);

        treePanel = new TreePanel();
        JScrollPane scrollPane = new JScrollPane(treePanel);
        scrollPane.getViewport().setBackground(new Color(245, 247, 250));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 232)),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        statusLabel = new JLabel("Ready. Insert a number to build your AVL tree.");
        statsLabel = new JLabel("Keys: 0 | Height: 0");
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statsLabel.setForeground(new Color(45, 60, 85));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(statsLabel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        
        insertButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                performOperation("Insert");
            }
            
        });
        searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                performOperation("Search");
            }
            
        });
        //searchButton.addActionListener(e -> performOperation("Search"));
        //rangeSearchButton.addActionListener(e -> performRangeSearch());
        deleteButton.addActionListener(e -> performOperation("Delete"));
        clearButton.addActionListener(e -> clearTree());
        valueField.addActionListener(e -> performOperation("Insert"));
        treeSelector.addActionListener(e -> handleTreeSelection());

        handleTreeSelection();
    }

    private JButton makeButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setMaximumSize(new Dimension(205, 39));
        button.setPreferredSize(new Dimension(205, 39));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private String selectedTree() {
        return (String) treeSelector.getSelectedItem();
    }

    private void handleTreeSelection() {
        String selected = selectedTree();
        insertButton.setEnabled(true);
        searchButton.setEnabled(true);
        deleteButton.setEnabled(true);
        clearButton.setEnabled(true);
        //rangeSearchButton.setEnabled("B+ Tree".equals(selected));
        treePanel.setTreeType(selected);
        treePanel.setHighlightedKey(null);
        refreshDisplayedTree();

        if ("AVL Tree".equals(selected)) {
            statusLabel.setText("AVL Tree selected. Insert, search, or delete an integer.");
        } else if ("B-Tree".equals(selected)) {
            statusLabel.setText("B-Tree selected (order 4). Insert, search, or delete an integer.");
        } else {
            statusLabel.setText("B+ Tree selected (order 4). All data keys are in linked leaves.");
        }
    }

    private void performOperation(String operation) {
        String selected = selectedTree();
        String text = valueField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an integer value.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            valueField.requestFocusInWindow();
            return;
        }

        final int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid 32-bit integer.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            valueField.selectAll();
            valueField.requestFocusInWindow();
            return;
        }

        boolean result;
        switch (operation) {
            case "Insert":
                valueField.setText("");
                if ("AVL Tree".equals(selected)) {result = avlTree.insert(value);}
                else if ("B-Tree".equals(selected)) result = bTree.insert(value);
                else result = bPlusTree.insert(value);
                treePanel.setHighlightedKey(result ? value : null);
                statusLabel.setText(result ? "Inserted " + value + " into " + selected + "."
                        : value + " already exists. Duplicate values are not added.");
                break;
            case "Search":
                if ("AVL Tree".equals(selected)) {result = avlTree.searchKey(value);}
                else if ("B-Tree".equals(selected)) result = bTree.contains(value);
                else result = bPlusTree.contains(value);
                treePanel.setHighlightedKey(result ? value : null);
                statusLabel.setText(result ? "Found " + value + " — highlighted in the tree."
                        : value + " was not found in the tree.");
                break;
            case "Delete":
                if ("AVL Tree".equals(selected)) result = avlTree.delete(value);
                else if ("B-Tree".equals(selected)) result = bTree.delete(value);
                else result = bPlusTree.delete(value);
                treePanel.setHighlightedKey(null);
                statusLabel.setText(result ? "Deleted " + value + " from " + selected + "."
                        : value + " is not in the tree, so nothing was deleted.");
                break;
            default:
                statusLabel.setText("Unknown operation.");
        }

        refreshDisplayedTree();
        valueField.requestFocusInWindow();
        valueField.selectAll();
    }

    /** Uses the value field as the inclusive start, then asks for the inclusive end. */
    // private void performRangeSearch() {
    //     if (!"B+ Tree".equals(selectedTree())) return;
    //     String startText = valueField.getText().trim();
    //     final int from;
    //     try {
    //         if (startText.isEmpty()) throw new NumberFormatException();
    //         from = Integer.parseInt(startText);
    //     } catch (NumberFormatException ex) {
    //         JOptionPane.showMessageDialog(this, "Enter a valid starting integer in the value field.",
    //                 "Invalid Range", JOptionPane.WARNING_MESSAGE);
    //         valueField.requestFocusInWindow();
    //         return;
    //     }

    //     String endText = JOptionPane.showInputDialog(this,
    //             "Enter the inclusive ending value (must be >= " + from + "):", "Range Search", JOptionPane.QUESTION_MESSAGE);
    //     if (endText == null) return;
    //     final int to;
    //     try {
    //         to = Integer.parseInt(endText.trim());
    //     } catch (NumberFormatException ex) {
    //         JOptionPane.showMessageDialog(this, "Please enter a valid 32-bit integer for the range end.",
    //                 "Invalid Range", JOptionPane.ERROR_MESSAGE);
    //         return;
    //     }
    //     if (from > to) {
    //         JOptionPane.showMessageDialog(this, "The start value must be less than or equal to the end value.",
    //                 "Invalid Range", JOptionPane.WARNING_MESSAGE);
    //         return;
    //     }

    //     List<Integer> found = bPlusTree.rangeSearch(from, to);
    //     Set<Integer> matches = new HashSet<>(found);
    //     treePanel.setHighlightedKeys(matches);
    //     statusLabel.setText("Range [" + from + ", " + to + "] returned " + found.size() + " key(s): " + found);
    //     valueField.requestFocusInWindow();
    //     valueField.selectAll();
    // }

    private void clearTree() {
        String selected = selectedTree();
        if ("AVL Tree".equals(selected)) avlTree.clear();
        else if ("B-Tree".equals(selected)) bTree.clear();
        else bPlusTree.clear();
        treePanel.setHighlightedKey(null);
        refreshDisplayedTree();
        statusLabel.setText(selected + " cleared.");
        valueField.requestFocusInWindow();
    }

    private void refreshDisplayedTree() {
        treePanel.setAvlRoot(avlTree.getRoot());
        treePanel.setBTreeRoot(bTree.getRoot());
        treePanel.setBPlusRoot(bPlusTree.getRoot());
        if ("AVL Tree".equals(selectedTree())) {
            statsLabel.setText("Keys: " + avlTree.size() + " | Height: " + avlTree.getHeight());
        } else if ("B-Tree".equals(selectedTree())) {
            statsLabel.setText("Keys: " + bTree.size() + " | Height: " + bTree.getHeight());
        } else {
            statsLabel.setText("Keys: " + bPlusTree.size() + " | Height: " + bPlusTree.getHeight()
                    + " | Leaves: " + bPlusTree.leafCount());
        }
    }

    
}
