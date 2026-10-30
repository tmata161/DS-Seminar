import java.util.ArrayList;
import java.util.List;

/**
 * B-Tree with given parameters
 */
public class BTree {
    public static final int ORDER = 5; //max number of children
    public static final int MIN_INTERNAL_CHILDREN = (int) Math.ceil(ORDER/2);
    public static final int MAX_KEYS = ORDER-1;
    public static final int MIN_KEYS = (int) Math.ceil(ORDER/2)-1;
    public static final int MIN_DEGREE = 2;

    public static class Node {
        public Integer highlightedKey;
        public final List<Integer> keys = new ArrayList<>();
        public final List<Node> children = new ArrayList<>();
        public boolean leaf = true;
    }

    private Node root;
    private int size;
    private static Node searchNode;
    public Node getRoot() { return root; }
    public int size() { return size; }
    public boolean isEmpty() { return root == null; }

    public int getHeight() { return height(root); }

    private int height(Node node) {
        if (node == null) return 0;
        if (node.leaf) return 1;
        return 1 + height(node.children.get(0));
    }

    public boolean contains(int key) {
        return search(root, key) != null;
    }

    private Node search(Node node, int key) {
        if (node == null) return null;
        int i = 0;
        while (i < node.keys.size() && key > node.keys.get(i)) i++;
        if (i < node.keys.size() && key == node.keys.get(i)) return node;
        if (node.leaf) return null;
        return search(node.children.get(i), key);
    }

    public boolean searchKey(Node node, int key) {
        if (node == null) return false;
        int i = 0;
        while (i < node.keys.size() && key > node.keys.get(i)) i++;
        if (i < node.keys.size() && key == node.keys.get(i)){ 
            searchNode=node;
            node.highlightedKey=node.keys.get(i);
            return node!=null;
        }
        if (node.leaf) return false;
        return searchKey(node.children.get(i), key);
    }

    public void resetSearchKey(){
        if(searchNode!=null){
            searchNode.highlightedKey=null;
        }
    }
    public boolean insert(int key) {
        if (contains(key)) return false;
        if (root == null) {
            root = new Node();
            root.keys.add(key);
            size = 1;
            return true;
        }
        if (root.keys.size() == MAX_KEYS) { //2 * MIN_DEGREE - 1
            Node newRoot = new Node();
            newRoot.leaf = false;
            newRoot.children.add(root);
            splitChild(newRoot, 0);
            root = newRoot;
        }
        insertNonFull(root, key);
        size++;
        return true;
    }

    private void insertNonFull(Node node, int key) {
        int i = node.keys.size() - 1;
        if (node.leaf) {
            node.keys.add(0);
            while (i >= 0 && key < node.keys.get(i)) {
                node.keys.set(i + 1, node.keys.get(i));
                i--;
            }
            node.keys.set(i + 1, key);
            return;
        }
        while (i >= 0 && key < node.keys.get(i)) i--;
        i++;
        if (node.children.get(i).keys.size() == MAX_KEYS) { //2 * MIN_DEGREE - 1
            splitChild(node, i);
            if (key > node.keys.get(i)) i++;
        }
        insertNonFull(node.children.get(i), key);
    }

    private void splitChild(Node parent, int childIndex) {
        Node full = parent.children.get(childIndex);
        Node sibling = new Node();
        sibling.leaf = full.leaf;

        int median = full.keys.get(MIN_KEYS);
        sibling.keys.addAll(new ArrayList<>(full.keys.subList(MIN_INTERNAL_CHILDREN, full.keys.size())));
        while (full.keys.size() > MIN_KEYS) {
            full.keys.remove(full.keys.size() - 1);
        }

        if (!full.leaf) {
            sibling.children.addAll(new ArrayList<>(full.children.subList(MIN_INTERNAL_CHILDREN, full.children.size())));
            while (full.children.size() > MIN_INTERNAL_CHILDREN) {
                full.children.remove(full.children.size() - 1);
            }
        }
        parent.keys.add(childIndex, median);
        parent.children.add(childIndex + 1, sibling);
    }

    public boolean delete(int key) {
        if (!contains(key)) return false;
        deleteFromNode(root, key);
        size--;
        if (root != null && root.keys.isEmpty()) {
            root = root.leaf ? null : root.children.get(0);
        }
        return true;
    }

    private void deleteFromNode(Node node, int key) {
        int idx = findKey(node, key);
        if (idx < node.keys.size() && node.keys.get(idx) == key) {
            if (node.leaf) {
                node.keys.remove(idx);
            } else {
                deleteFromInternalNode(node, idx);
            }
            return;
        }

        if (node.leaf) return;
        boolean wasLastChild = idx == node.keys.size();
        if (node.children.get(idx).keys.size() < MIN_INTERNAL_CHILDREN) {
            fillChild(node, idx);
        }
        if (wasLastChild && idx > node.keys.size()) {
            deleteFromNode(node.children.get(idx - 1), key);
        } else {
            deleteFromNode(node.children.get(idx), key);
        }
    }

    private int findKey(Node node, int key) {
        int idx = 0;
        while (idx < node.keys.size() && node.keys.get(idx) < key) idx++;
        return idx;
    }

    private void deleteFromInternalNode(Node node, int idx) {
        int key = node.keys.get(idx);
        Node left = node.children.get(idx);
        Node right = node.children.get(idx + 1);
        if (left.keys.size() >= MIN_INTERNAL_CHILDREN) {
            int predecessor = predecessor(left);
            node.keys.set(idx, predecessor);
            deleteFromNode(left, predecessor);
        } else if (right.keys.size() >= MIN_INTERNAL_CHILDREN) {
            int successor = successor(right);
            node.keys.set(idx, successor);
            deleteFromNode(right, successor);
        } else {
            mergeChildren(node, idx);
            deleteFromNode(left, key);
        }
    }

    private int predecessor(Node node) {
        while (!node.leaf) node = node.children.get(node.children.size() - 1);
        return node.keys.get(node.keys.size() - 1);
    }

    private int successor(Node node) {
        while (!node.leaf) node = node.children.get(0);
        return node.keys.get(0);
    }

    private void fillChild(Node parent, int idx) {
        if (idx > 0 && parent.children.get(idx - 1).keys.size() >= MIN_INTERNAL_CHILDREN) {
            borrowFromPrevious(parent, idx);
        } else if (idx < parent.keys.size()
                && parent.children.get(idx + 1).keys.size() >= MIN_INTERNAL_CHILDREN) {
            borrowFromNext(parent, idx);
        } else {
            if (idx < parent.keys.size()) mergeChildren(parent, idx);
            else mergeChildren(parent, idx - 1);
        }
    }

    private void borrowFromPrevious(Node parent, int idx) {
        Node child = parent.children.get(idx);
        Node sibling = parent.children.get(idx - 1);
        child.keys.add(0, parent.keys.get(idx - 1));
        if (!child.leaf) {
            child.children.add(0, sibling.children.remove(sibling.children.size() - 1));
        }
        parent.keys.set(idx - 1, sibling.keys.remove(sibling.keys.size() - 1));
    }

    private void borrowFromNext(Node parent, int idx) {
        Node child = parent.children.get(idx);
        Node sibling = parent.children.get(idx + 1);
        child.keys.add(parent.keys.get(idx));
        if (!child.leaf) {
            child.children.add(sibling.children.remove(0));
        }
        parent.keys.set(idx, sibling.keys.remove(0));
    }

    private void mergeChildren(Node parent, int idx) {
        Node left = parent.children.get(idx);
        Node right = parent.children.get(idx + 1);
        left.keys.add(parent.keys.remove(idx));
        left.keys.addAll(right.keys);
        if (!right.leaf) left.children.addAll(right.children);
        parent.children.remove(idx + 1);
    }

    public void clear() {
        root = null;
        size = 0;
    }
}
