import java.util.ArrayList;
import java.util.List;

/**
 * B+ Tree with given parameters
 */
public class BPlusTree {
    public static final int ORDER = 5;
    private static final int MAX_KEYS = ORDER - 1;
    private static final int MIN_LEAF_KEYS = (int) (Math.ceil(ORDER/2)-1);
    private static final int MIN_INTERNAL_CHILDREN = (int)(Math.ceil(ORDER/2));

    public static class Node {
        public Integer highlightedKey;
        public final boolean leaf;
        public final List<Integer> keys = new ArrayList<>();
        public final List<Node> children = new ArrayList<>();
        public Node next;

        private Node(boolean leaf) {
            this.leaf = leaf;
        }
    }

    private static Node searchNode;
    private Node root;
    private int size;
    public Node getRoot() { return root; }
    public int size() { return size; }
    public boolean isEmpty() { return root == null; }
    public int getHeight() { return height(root); }

    public int leafCount() {
        int count = 0;
        Node leaf = firstLeaf();
        while (leaf != null) {
            count++;
            leaf = leaf.next;
        }
        return count;
    }

    public boolean contains(int key) {
        Node leaf = findLeaf(key);
        if (leaf == null) return false;
        int index = lowerBound(leaf.keys, key);
        if(index < leaf.keys.size() && leaf.keys.get(index)==key){
            searchNode=leaf;
            searchNode.highlightedKey=leaf.keys.get(index);
        }
        return index < leaf.keys.size() && leaf.keys.get(index) == key;
    }


    public void resetSearchKey(){
        if(searchNode!=null){
            searchNode.highlightedKey=null;
            searchNode=null;
        }
    }
    public boolean insert(int key) {
        if (contains(key)) return false;
        if (root == null) {
            root = new Node(true);
            root.keys.add(key);
            size = 1;
            return true;
        }

        Node splitRight = insertRecursive(root, key);
        if (splitRight != null) {
            Node newRoot = new Node(false);
            newRoot.children.add(root);
            newRoot.children.add(splitRight);
            refreshSeparators(newRoot);
            root = newRoot;
        }
        size++;
        return true;
    }

    /** Returns the new right sibling when this node splits, or null otherwise. */
    private Node insertRecursive(Node node, int key) {
        if (node.leaf) {
            int index = lowerBound(node.keys, key);
            node.keys.add(index, key);
            if (node.keys.size() <= MAX_KEYS) return null;

            Node right = new Node(true);
            int splitAt = node.keys.size() / 2; // 4 keys -> 2 keys on each side
            right.keys.addAll(new ArrayList<>(node.keys.subList(splitAt, node.keys.size())));
            node.keys.subList(splitAt, node.keys.size()).clear();
            right.next = node.next;
            node.next = right;
            return right;
        }

        int childIndex = childIndexFor(node, key);
        Node splitRight = insertRecursive(node.children.get(childIndex), key);
        if (splitRight != null) node.children.add(childIndex + 1, splitRight);
        refreshSeparators(node);

        if (node.children.size() <= ORDER) return null;

        Node right = new Node(false);
        int splitAt = node.children.size() / 2; 
        right.children.addAll(new ArrayList<>(node.children.subList(splitAt, node.children.size())));
        node.children.subList(splitAt, node.children.size()).clear();
        refreshSeparators(node);
        refreshSeparators(right);
        return right;
    }

    public boolean delete(int key) {
        if (!contains(key)) return false;
        deleteRecursive(root, key);
        size--;

        if (root != null && !root.leaf && root.children.size() == 1) {
            root = root.children.get(0);
        }
        if (root != null && root.leaf && root.keys.isEmpty()) {
            root = null;
        }
        return true;
    }

    private void deleteRecursive(Node node, int key) {
        if (node.leaf) {
            int index = lowerBound(node.keys, key);
            if (index < node.keys.size() && node.keys.get(index) == key) node.keys.remove(index);
            return;
        }

        int childIndex = childIndexFor(node, key);
        Node child = node.children.get(childIndex);
        deleteRecursive(child, key);

        if (isUnderfull(child)) rebalanceChild(node, childIndex);
        refreshSeparators(node);
    }

    private boolean isUnderfull(Node node) {
        return node.leaf
                ? node.keys.size() < MIN_LEAF_KEYS
                : node.children.size() < MIN_INTERNAL_CHILDREN;
    }

    private void rebalanceChild(Node parent, int index) {
        Node child = parent.children.get(index);
        if (child.leaf) {
            // Borrow from the left leaf if it has an extra key.
            if (index > 0) {
                Node left = parent.children.get(index - 1);
                if (left.keys.size() > MIN_LEAF_KEYS) {
                    child.keys.add(0, left.keys.remove(left.keys.size() - 1));
                    refreshSeparators(parent);
                    return;
                }
            }
            // Otherwise borrow from the right leaf if possible.
            if (index + 1 < parent.children.size()) {
                Node right = parent.children.get(index + 1);
                if (right.keys.size() > MIN_LEAF_KEYS) {
                    child.keys.add(right.keys.remove(0));
                    refreshSeparators(parent);
                    return;
                }
            }
            // Neither sibling can lend a key, so merge leaves and retain the chain.
            if (index > 0) {
                Node left = parent.children.get(index - 1);
                left.keys.addAll(child.keys);
                left.next = child.next;
                parent.children.remove(index);
            } else if (index + 1 < parent.children.size()) {
                Node right = parent.children.get(index + 1);
                child.keys.addAll(right.keys);
                child.next = right.next;
                parent.children.remove(index + 1);
            }
            refreshSeparators(parent);
            return;
        }

        // Rebalance an underfull internal node by moving child pointers.
        if (index > 0) {
            Node left = parent.children.get(index - 1);
            if (left.children.size() > MIN_INTERNAL_CHILDREN) {
                child.children.add(0, left.children.remove(left.children.size() - 1));
                refreshSeparators(left);
                refreshSeparators(child);
                refreshSeparators(parent);
                return;
            }
        }
        if (index + 1 < parent.children.size()) {
            Node right = parent.children.get(index + 1);
            if (right.children.size() > MIN_INTERNAL_CHILDREN) {
                child.children.add(right.children.remove(0));
                refreshSeparators(right);
                refreshSeparators(child);
                refreshSeparators(parent);
                return;
            }
        }
        if (index > 0) {
            Node left = parent.children.get(index - 1);
            left.children.addAll(child.children);
            refreshSeparators(left);
            parent.children.remove(index);
        } else if (index + 1 < parent.children.size()) {
            Node right = parent.children.get(index + 1);
            child.children.addAll(right.children);
            refreshSeparators(child);
            parent.children.remove(index + 1);
        }
        refreshSeparators(parent);
    }

    /** Returns all data keys in the inclusive range [from, to], in sorted order. */
    // public List<Integer> rangeSearch(int from, int to) {
    //     List<Integer> result = new ArrayList<>();
    //     if (from > to || root == null) return result;
    //     Node leaf = findLeaf(from);
    //     if (leaf == null) return result;
    //     while (leaf != null) {
    //         for (int key : leaf.keys) {
    //             if (key < from) continue;
    //             if (key > to) return result;
    //             result.add(key);
    //         }
    //         leaf = leaf.next;
    //     }
    //     return result;
    // }

    public void clear() {
        root = null;
        size = 0;
    }

    private Node findLeaf(int key) {
        Node current = root;
        while (current != null && !current.leaf) {
            current = current.children.get(childIndexFor(current, key));
        }
        return current;
    }

    private Node firstLeaf() {
        Node current = root;
        while (current != null && !current.leaf) current = current.children.get(0);
        return current;
    }

    private int childIndexFor(Node internal, int key) {
        // Separators store the smallest key of the right child; equality goes right.
        int index = 0;
        while (index < internal.keys.size() && key >= internal.keys.get(index)) index++;
        return index;
    }

    private int lowerBound(List<Integer> keys, int key) {
        int low = 0, high = keys.size();
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (keys.get(mid) < key) low = mid + 1;
            else high = mid;
        }
        return low;
    }

    private void refreshSeparators(Node node) {
        if (node == null || node.leaf) return;
        node.keys.clear();
        for (int i = 1; i < node.children.size(); i++) {
            node.keys.add(firstKey(node.children.get(i)));
        }
    }

    private int firstKey(Node node) {
        Node current = node;
        while (!current.leaf) current = current.children.get(0);
        return current.keys.get(0);
    }

    private int height(Node node) {
        if (node == null) return 0;
        if (node.leaf) return 1;
        return 1 + height(node.children.get(0));
    }
}
