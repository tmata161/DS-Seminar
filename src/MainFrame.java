import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame implements ActionListener{
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
    private final JButton deleteButton;
    private final JButton clearButton;
    
    String helpTextString="<html><div style='width:180px'>AVL Tree:self-balancing binary search tree.<br><br>B-Tree: B-Tree of order m; nodes can contain up to m-1 sorted keys.<br><br>B+ Tree: In B+ Tree data is stored in linked leaves; internal keys guide the search. Use Range Search to query an interval.</div></html>";
    public static Integer setSearchedKey;
    //constructor of main frame class
    public MainFrame() {

        //initializing the methods of AVL, B AND B+ Trees
        avlTree = new AVLTree();
        bTree = new BTree();
        bPlusTree = new BPlusTree();

        //setting up the properties of the window
        setTitle("Tree Data Structure Visualizer");
        setSize(1100, 740);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        setIconImage(new ImageIcon(Main.class.getResource("logo.png")).getImage());

        //title text
        JLabel title = new JLabel("Tree Data Structure Visualizer", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setOpaque(true);
        title.setBackground(new Color(25, 45, 85));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));
        add(title, BorderLayout.NORTH);


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
        deleteButton = makeButton("Delete", new Color(217, 83, 79));
        clearButton = makeButton("Clear Tree", new Color(74, 85, 104));

        JLabel helpTitle = new JLabel("About Trees");
        helpTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel helpText = new JLabel(helpTextString);
        helpText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        helpText.setForeground(new Color(75, 85, 100));

        //left side control bar
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(Color.WHITE);
        controls.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 225, 232)),
                BorderFactory.createEmptyBorder(18, 15, 18, 15)));
        controls.setPreferredSize(new Dimension(235, 0));
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
        controls.add(Box.createVerticalStrut(9));
        controls.add(deleteButton);
        controls.add(Box.createVerticalStrut(9));
        controls.add(clearButton);
        controls.add(Box.createVerticalStrut(24));
        controls.add(helpTitle);
        controls.add(Box.createVerticalStrut(8));
        controls.add(helpText);
        add(controls, BorderLayout.WEST);

        treePanel = new TreePanel();

        JScrollPane scrollPane = new JScrollPane(treePanel);
        scrollPane.getViewport().setBackground(new Color(245, 247, 250));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        //bottom panel section
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 232)),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        statusLabel = new JLabel();
        statsLabel = new JLabel("Keys: 0 | Height: 0");
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statsLabel.setForeground(new Color(45, 60, 85));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(statsLabel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        insertButton.addActionListener(this);
        searchButton.addActionListener(this);
        deleteButton.addActionListener(this);
        clearButton.addActionListener(this);
        valueField.addActionListener(this);
        treeSelector.addActionListener(this);

        handleTreeSelection();
    }

    //creates the button object and set its properties
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
        button.setEnabled(true);
        return button;
    }

    private String selectedTree() {
        return (String) treeSelector.getSelectedItem();
    }

    private void handleTreeSelection() {
        String selected = selectedTree();
        treePanel.setTreeType(selected);
        refreshDisplayedTree();

        if ("AVL Tree".equals(selected)) {
            statusLabel.setText("AVL Tree selected. Insert, search, or delete an integer.");
        } else if ("B-Tree".equals(selected)) {
            statusLabel.setText("B-Tree selected (order "+BTree.ORDER+"). Insert, search, or delete an integer.");
        } else {
            statusLabel.setText("B+ Tree selected (order "+BPlusTree.ORDER+"). All data keys are in linked leaves.");
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
        avlTree.resetHighlightedNodes();
        bTree.resetSearchKey();
        bPlusTree.resetSearchKey();
        setSearchedKey=null;
        boolean result;
        switch (operation) {
            case "Insert":
                valueField.setText("");
                if ("AVL Tree".equals(selected)) {result = avlTree.insert(value);}
                else if ("B-Tree".equals(selected)) result = bTree.insert(value);
                else result = bPlusTree.insert(value);
                statusLabel.setText(result ? "Inserted " + value + " into " + selected + "."
                        : value + " already exists. Duplicate values are not added.");
                break;
            case "Search":
                if ("AVL Tree".equals(selected)) {result = avlTree.searchKey(value);}
                else if ("B-Tree".equals(selected)){
                     result = bTree.searchKey(bTree.getRoot(), value);
                     setCurrentValue(text);
                }
                else {
                    result = bPlusTree.contains(value);
                    setCurrentValue(text);
                }
                statusLabel.setText(result ? "Found " + value + " — highlighted in the tree."
                        : value + " was not found in the tree.");
                break;
            case "Delete":
                if ("AVL Tree".equals(selected)) result = avlTree.delete(value);
                else if ("B-Tree".equals(selected)) result = bTree.delete(value);
                else result = bPlusTree.delete(value);
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

    private void clearTree() {
        String selected = selectedTree();
        if ("AVL Tree".equals(selected)) avlTree.clear();
        else if ("B-Tree".equals(selected)) bTree.clear();
        else bPlusTree.clear();
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand()=="Clear Tree")clearTree();
        else if(e.getActionCommand()=="Insert" || e.getActionCommand()=="Search" || e.getActionCommand()=="Delete")
        performOperation(e.getActionCommand());
        else if(Character.isDigit(e.getActionCommand().charAt(0)))
            performOperation("Insert");
        else handleTreeSelection();
    }

    private void setCurrentValue(String text){
        try {
            setSearchedKey = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
           ex.printStackTrace();
        }
    }

    
}
