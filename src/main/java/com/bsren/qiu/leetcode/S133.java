package com.bsren.qiu.leetcode;

import com.bsren.qiu.leetcode.tree.Node;

import java.util.*;

public class S133 {

    //深度优先搜索
    Map<Node,Node> map = new HashMap<>();

    public Node cloneGraph(Node node) {
        if(node==null){
            return null;
        }
        if(map.containsKey(node)){
            return map.get(node);
        }
        Node newNode = new Node(node.val,new ArrayList<>());
        map.put(node,newNode);
        for (Node neighbor : node.neighbors) {
            newNode.neighbors.add(cloneGraph(neighbor));
        }
        return newNode;
    }



    //广度优先搜索
    public Node cloneGraph1(Node node){
        if(node==null){
            return null;
        }
        Deque<Node> deque = new LinkedList<>();
        Map<Node,Node> map = new HashMap<>();
        deque.addLast(node);
        map.put(node,new Node(node.val,new ArrayList<>()));
        while (!deque.isEmpty()){
            Node first = deque.removeFirst();
            for (Node neighbor : first.neighbors) {
                if(!map.containsKey(neighbor)){
                    map.put(neighbor,new Node(neighbor.val,new ArrayList<>()));
                    deque.addLast(neighbor);
                }
                map.get(first).neighbors.add(map.get(neighbor));
            }
        }
        return map.get(node);
    }
}
