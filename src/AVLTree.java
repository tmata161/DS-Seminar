/**
 * AVL tree implementation supporting insertion, searching, deletion and balancing.
 * No package declaration is used so this class works with the simple src/ layout.
 */
public class AVLTree {
    public static class Node {
        public int key;
        public int height;
        public Node left;
        public Node right;
        //highlight the nodes that leads to the node that has been searched
        public int highlightedNodes;
        Node(int key) {
            this.key = key;
            this.height = 1;
            this.highlightedNodes=0;
        }
    }

    private Node root;

    public Node getRoot() {
        return root;
    }

    public boolean insert(int key) {
        //if key already present then just return false
        if (contains(key)) {
            return false;
        }
        //if key not present then insert a key
        root = insert(root, key);
        return true;
    }

    //this method checks if a key is present or not
    public void resetHighlightedNodes(Node cRoot){
        if(cRoot==null) return;
        resetHighlightedNodes(cRoot.left);
        cRoot.highlightedNodes=0;
        resetHighlightedNodes(cRoot.right);
    }
    public boolean contains(int key) {
        Node current = root;
        while (current != null) {
            current.highlightedNodes=1;
            if (key == current.key) {
                return true;
            }
            current = key < current.key ? current.left : current.right;
        }
        return false;
    }

    public boolean delete(int key) {
        //if key is not present in a tree
        if (!contains(key)) {
            return false;
        }
        root = delete(root, key);
        return true;
    }

    public void clear() {
        //rest of the nodes will automatically be deleted by gc
        root = null;
    }

    public int size() {
        return size(root);
    }

    public int getHeight() {
        return height(root);
    }

    private int size(Node node) {
        if (node == null) {
            return 0;
        }
        return 1 + size(node.left) + size(node.right);
    }

    private Node insert(Node node, int key) {
        //if no nodes are present
        if (node == null) {
            return new Node(key);
        }

        if (key < node.key) {
            node.left = insert(node.left, key);
        } else if (key > node.key) {
            node.right = insert(node.right, key);
        } else {
            return node;
        }

        updateHeight(node);
        return balance(node);
    }

    private Node delete(Node node, int key) {
        if (node == null) {
            return null;
        }

        if (key < node.key) {
            node.left = delete(node.left, key);
        } else if (key > node.key) {
            node.right = delete(node.right, key);
        } else {
            if (node.left == null || node.right == null) {
                node = (node.left != null) ? node.left : node.right;
            } else {
                Node successor = minimum(node.right);
                node.key = successor.key;
                node.right = delete(node.right, successor.key);
            }
        }

        if (node == null) {
            return null;
        }

        updateHeight(node);
        return balance(node);
    }

    private Node minimum(Node node) {
        Node current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    private Node balance(Node node) {
        int factor = balanceFactor(node);

        // Left-heavy subtree.
        if (factor > 1) {
            if (balanceFactor(node.left) < 0) {
                node.left = rotateLeft(node.left); // Left-Right case
            }
            return rotateRight(node); // Left-Left case
        }

        // Right-heavy subtree.
        if (factor < -1) {
            if (balanceFactor(node.right) > 0) {
                node.right = rotateRight(node.right); // Right-Left case
            }
            return rotateLeft(node); // Right-Right case
        }

        return node;
    }

    private Node rotateRight(Node top) {
        Node newTop = top.left;
        Node movedSubtree = newTop.right;

        newTop.right = top;
        top.left = movedSubtree;

        updateHeight(top);
        updateHeight(newTop);
        return newTop;
    }

    private Node rotateLeft(Node top) {
        Node newTop = top.right;
        Node movedSubtree = newTop.left;

        newTop.left = top;
        top.right = movedSubtree;

        updateHeight(top);
        updateHeight(newTop);
        return newTop;
    }

    private void updateHeight(Node node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private int height(Node node) {
        return node == null ? 0 : node.height;
    }

    public int balanceFactor(Node node) {
        if (node == null) {
            return 0;
        }
        return height(node.left) - height(node.right);
    }
}
