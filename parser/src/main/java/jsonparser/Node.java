package jsonparser;

public class Node {
    Object data;
    Node nextNode;
    Node prevNode;
    boolean isNull = true;

    Node() {

    }

    Node(Object data) {
        this.data = data;
        this.isNull = false;
    }

    void addNode(Node node) {
        if (this.nextNode == null)
            this.nextNode = node;
        else {
            this.nextNode.addNode(node);
        }
    }

}
