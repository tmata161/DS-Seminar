TREE DATA STRUCTURE VISUALIZER - AVL + B-TREE + B+ TREE

Requirements:
- Java JDK 8 or newer
- VS Code, IntelliJ IDEA, or another Java-capable editor

Project files:
  src/Main.java       - Application entry point
  src/MainFrame.java  - GUI, tree selection, controls, operations, and range search
  src/TreePanel.java  - Draws the AVL Tree, B-Tree, and B+ Tree
  src/AVLTree.java    - AVL insertion, search, deletion, and rotations
  src/BTree.java      - B-Tree insertion, search, deletion, splitting, merging, and borrowing
  src/BPlusTree.java  - B+ Tree insertion, search, deletion, linked leaves, and range search
  out/                - Compiled class output directory

Run in PowerShell from this project folder:

  javac -d .\out .\src\Main.java .\src\MainFrame.java .\src\TreePanel.java .\src\AVLTree.java .\src\BTree.java .\src\BPlusTree.java
  java -cp .\out Main

Implemented features:
- AVL Tree: insertion, search, deletion, four balancing rotation cases, height and balance factor
- B-Tree: order 4, insertion, search, deletion, node splits, merges, and borrowing
- B+ Tree: order 4, internal separator keys, all data stored in leaves, leaf splitting, deletion rebalancing,
  linked leaf nodes, range search, key highlighting, tree height and leaf count
- Duplicate keys are not inserted
- Search results are highlighted; range search highlights all matching leaf keys
- Scrollable visualization canvas for larger trees
- Clear Tree button works for all three tree modes

Using B+ Tree Range Search:
1. Select B+ Tree from the dropdown.
2. Enter the inclusive starting value in the input field.
3. Click "Range Search (B+)" and enter the inclusive ending value in the dialog.
4. Matching keys in the range are highlighted, and the matching sorted values are shown in the status bar.
