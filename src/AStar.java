import javafx.util.Pair;

import java.util.*;

public class AStar {
    enum heuristic{
        MANHATTANDISTANCE_HEURISTIC,
        MISPLACEDTILES_HEURISTIC
    }

    public PriorityQueue<Node> fringePriorityQueue;

    public static void main(String args[]){
        System.out.println("Please Enter the initial state row wise");
        Node initialStateNode = createNode();
        System.out.println("Please Enter the Goal state row wise");
        Node goalStateNode = createNode();

        if(!isValidInput(initialStateNode.getState()) || !isValidInput(goalStateNode.getState()))
            return;

        AStar aStar = new AStar();

        aStar.findBestPath(initialStateNode, goalStateNode, heuristic.MANHATTANDISTANCE_HEURISTIC);
        aStar.findBestPath(initialStateNode, goalStateNode, heuristic.MISPLACEDTILES_HEURISTIC);

    }

    /**
     * Takes a node state as an input and returns true if it is a valid input else false.
     * @param state is a two dimensional integer array which represents the state of the board.
     * @return a boolean true if input is valid and false if it is invalid.
     */
    private static boolean isValidInput(int[][] state) {
        Set<Integer> validSet = new HashSet<>();
        for(int i =0;i<9;i++){
            validSet.add(i);
        }
        for(int i=0;i<state.length;i++){
            for(int j=0;j<state.length;j++){
                if(validSet.contains(state[i][j])){
                    validSet.remove(state[i][j]);
                } else {
                    System.out.println("Please Enter Unique Values");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Takes input from the user and generates a node for the state
     * @return Node generated for the input state
     */

    public static Node createNode(){
        Scanner sc = new Scanner(System.in);
        int[][] state = new int[3][3];
            for(int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    state[i][j] = sc.nextInt();
                }
            }

            Node node = new Node();
            node.setState(state);
        return node;
    }

    /**
     * Takes the initial state and goal state from the user and computes and prints the best path found
     * @param initialStateNode Node representing initial state
     * @param goalStateNode representing final state
     * @param typeOfHeuristic representing the type of heuristic
     */

    public void findBestPath(Node initialStateNode, Node goalStateNode, heuristic typeOfHeuristic){
        int stateID = 0;
        int noOfNodesGenerated = 1;

        Map<Integer, Node> exploredNodes = new HashMap<>();
        System.out.println("###############" +
                (typeOfHeuristic == heuristic.MANHATTANDISTANCE_HEURISTIC ? "MANHATTAN DISTANCE HEURISTIC" : "MISPLACED TILES HEURISTIC") + "###############");


        initialStateNode.setStateID(++stateID);
        initialStateNode.setParentID(0);
        initialStateNode.setGcost(0);
        setHcostAndFcost(initialStateNode, goalStateNode, typeOfHeuristic);
        fringePriorityQueue = new PriorityQueue<>(200, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.getFcost() - o2.getFcost();
            }
        });
        fringePriorityQueue.add(initialStateNode);

        while (!fringePriorityQueue.isEmpty()){
            Node expandingNode = fringePriorityQueue.poll();
            exploredNodes.put(expandingNode.getStateID(), expandingNode);

            Pair<Integer, Integer> emptyTile = findEmptyTile(expandingNode);

            List<Pair<Integer, Integer>> noOfChildStates = findPossibleMovementsOfEmptyTile(expandingNode,emptyTile.getKey(),emptyTile.getValue());

            if(expandingNode.getHcost() == 0) {
               Stack<Node> bestpath = calculateBestPath(expandingNode, exploredNodes);

                System.out.println("------------ Best Path from initial state to goal state--------------");
                int pathCost = bestpath.size() - 1;
                printBestPath(bestpath);
                System.out.println();
                System.out.println("No of Nodes Generated: " + noOfNodesGenerated);
                System.out.println("No of Nodes Explored: " + exploredNodes.size());
                System.out.println("Path Cost: " + pathCost);
                System.out.println();
                break;
            }


            for(int i=0;i<noOfChildStates.size();i++){
                Node childNode = createChildStateNode(expandingNode,emptyTile.getKey(),emptyTile.getValue(),noOfChildStates.get(i).getKey(), noOfChildStates.get(i).getValue());
                childNode.setStateID(++stateID);
                ++noOfNodesGenerated;

                // if the child is not in explored or fringe, we set the heuristic values// and add it to fringe
                if (!isThisChildInExplored(exploredNodes, childNode) && !isThisChildInPriorityQueue(fringePriorityQueue, childNode)) {
                    setHcostAndFcost(childNode, goalStateNode, typeOfHeuristic);
                    fringePriorityQueue.add(childNode);
                }

            }

        }


    }

    /**
     *Checks if the child state node is in the Fringe(Priority Queue)
     * @param fringePriorityQueue fringe which is a priority queue data structure
     * @param childNode the child Node to be checked
     * @return returns true if the child is found in fringe else returns false
     */
    boolean isThisChildInPriorityQueue(PriorityQueue<Node> fringePriorityQueue, Node childNode){
        Iterator<Node> iterator = fringePriorityQueue.iterator();
        Node node;

        while(iterator.hasNext()){
            node = iterator.next();
            if (isBothNodesEqual(node, childNode))
                return true;
        }

        return false;
    }

    /**
     * Checks if the node is present in Explored Nodes.
     * @param exploredNodes contains the States Nodes that are explored
     * @param childNode the Node to be checked
     * @return true if the node is present in explored Nodes else return false
     */
    boolean isThisChildInExplored( Map<Integer, Node> exploredNodes, Node childNode){
        for (Map.Entry<Integer, Node> e : exploredNodes.entrySet()) {
            Node exploredNode =  e.getValue();
            if (isBothNodesEqual(childNode, exploredNode))
                return true;
        }
        return false;
    }

    /**
     * Computes and sets both heuristic cost and the cost function f(n) based on the type of heuristic
     * @param initialStateNode node representing the inital state
     * @param goalStateNode node representing the goal state
     * @param typeOfHeuristic represents the type of heuristic
     */

    public void setHcostAndFcost(Node initialStateNode, Node goalStateNode, heuristic typeOfHeuristic){
        initialStateNode.setHcost(typeOfHeuristic == heuristic.MANHATTANDISTANCE_HEURISTIC
                ? calculateManhattanDistance(initialStateNode, goalStateNode)
                : calculateMisplacedTiles(initialStateNode, goalStateNode));
        initialStateNode.setFcost(initialStateNode.getGcost() + initialStateNode.getHcost());
    }

    /**
     *Checks if both nodes contains same state values
     * @param node1 represents the first operand
     * @param node2 represents the second operand
     * @return return true if both nodes contains the same state values else false
     */
    private boolean isBothNodesEqual(Node node1, Node node2) {
        int[][] node1State = node1.getState();
        int[][] node2State = node2.getState();
        for (int i = 0; i < node1State.length; i++)
            for (int j = 0; j < node2State.length; j++)
                if (node1State[i][j] != node2State[i][j])
                    return false;
        return true;
    }

    /**
     * creates the child node by moving the empty tile to new cell(represented by new row and new column)
     * @param expandingNode represents the node which is expanding to create the child
     * @param oldRow old row value of the empty tile
     * @param oldColumn old column value of the empty tile
     * @param newRow new row value of the empty tile
     * @param newColumn new column value of the empty tile
     * @return returns the Child Node created by moving the empty tile to new cell (represented by new row and new column)
     */

    public Node createChildStateNode(Node expandingNode, int oldRow, int oldColumn, int newRow, int newColumn){

        Node childNode = new Node();
        int[][] childNodeState = new int[3][3];

        // First we create the child by copying the parent state values
        for (int i = 0; i < expandingNode.getState().length; i++)
            for (int j = 0; j < expandingNode.getState().length; j++)
                childNodeState[i][j] = expandingNode.getState()[i][j];


        // move empty tile to new position in the created child
        int temp = childNodeState[oldRow][oldColumn];
        childNodeState[oldRow][oldColumn] = childNodeState[newRow][newColumn];
        childNodeState[newRow][newColumn] = temp;

        childNode.setState(childNodeState);
        childNode.setGcost(expandingNode.getGcost() + 1);
        childNode.setParentID(expandingNode.getStateID());
        return childNode;

    }

    /**
     * find the cell containing the empty tile in a node
     * @param node node in which empty tile is searched
     * @return returns a pair object containing empty tile's row as key and column as value
     */

    public Pair<Integer, Integer> findEmptyTile(Node node){
        int[][] expandingNodeState = node.getState();
        int p = 0, q = 0;
        parentLoop: for (p = 0; p < expandingNodeState.length; p++) {
            for (q = 0; q < expandingNodeState.length; q++) {
                if (expandingNodeState[p][q] == 0)
                    break parentLoop;
            }
        }

        return new Pair<>(p,q);
    }

    /**
     * Computes and returns a list of possible pairs of values that represents the location to which the empty tile can be moved
     * @param expandingNode node based on which new possible empty tile movements are computed
     * @param row row value of the empty tile
     * @param column column value of the empty tile
     * @return retuns list of Pairs of values representing the new empty tile locations
     */

    public List<Pair<Integer, Integer>> findPossibleMovementsOfEmptyTile(Node expandingNode, int row, int column){
        int[][] initialState = expandingNode.getState();
        List<Pair<Integer, Integer>> possibleMovementsOfEmptyTile = new ArrayList<>();
        // DOWN
        if ((row + 1) < initialState.length)
            possibleMovementsOfEmptyTile.add(new Pair<>(row+1, column));

        // UP
        if ((row - 1) >= 0)
            possibleMovementsOfEmptyTile.add(new Pair<>(row-1, column));

        // RIGHT
        if ((column + 1) < initialState.length)
            possibleMovementsOfEmptyTile.add(new Pair<>(row, column+1));

        // LEFT
        if ((column - 1) >= 0)
            possibleMovementsOfEmptyTile.add(new Pair<>(row, column-1));

        return possibleMovementsOfEmptyTile;
    }

    /**
     * calculates the manhattan distance value based on the initalstate and goal state
     * @param initialStateNode node representing the intial state
     * @param goalStateNode node representing the goal state
     * @return  returns the manhattan distance value
     */
    public int calculateManhattanDistance(Node initialStateNode, Node goalStateNode){
        int[][] initialState = initialStateNode.getState();
        int[][] goalState = goalStateNode.getState();
        int manhattanDistance = 0;
        int p = 0, q = 0, r = 0, s = 0;
        for (int a = 1; a <= 8; a++) {
            parentLoop : for (p = 0; p < initialState.length; p++)
                for (q = 0; q < initialState.length; q++)
                    if (initialState[p][q] == a)
                        break parentLoop;
            parentLoop2: for (r = 0; r < goalState.length; r++)
                for (s = 0; s < goalState.length; s++)
                    if (goalState[r][s] == a)
                        break parentLoop2;
            manhattanDistance = manhattanDistance + (Math.abs(p - r) + Math.abs(q - s));
        }
        return manhattanDistance;
    }

    /**
     * calculates the misplaced tiles value based on the initalstate and goal state
     * @param initialStateNode  node representing the intial state
     * @param goalStateNode node representing the goal state
     * @return returns the misplaced tiles value
     */
    public int calculateMisplacedTiles(Node initialStateNode, Node goalStateNode){
        int[][] initialState = initialStateNode.getState();
        int[][] goalState = goalStateNode.getState();
        int misplacedTiles = 0;
        for (int i = 0; i < initialState.length; i++)
            for (int j = 0; j < initialState.length; j++) {
                if (initialState[i][j] != goalState[i][j] && initialState[i][j] > 0) {
                    misplacedTiles++;
                }
            }
        return misplacedTiles;
    }

    /**
     *prints the node in 8 puzzle format
     * @param node Node that needs to printed
     */
    public static void printPuzzle(Node node){
        int[][] state = node.getState();

        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state.length; j++)
                System.out.print("\t" + state[i][j]);
            System.out.println();
        }
        System.out.println("g(n)= " + node.getGcost() + "\th(n)= " + node.getHcost()
                + "\tf(n)= " + node.getFcost());
    }

    /**
     * Computes the path from inital to goal state using the explored nodes
     * @param expandingNode node using which we construct the path by traversing upwards
     * @param exploredNodes contains the explored nodes
     * @return a stack representing the path from intial to goal state
     */

    public Stack<Node> calculateBestPath(Node expandingNode, Map<Integer, Node> exploredNodes){
        Stack<Node> bestPath = new Stack<>();
        bestPath.push(expandingNode);
        Node currentNode = expandingNode;
        int parentID = currentNode.getParentID();

        while (parentID !=0) {
            bestPath.push(exploredNodes.get(parentID));
            currentNode = exploredNodes.get(parentID);
            parentID = currentNode.getParentID();
        }

        return bestPath;
    }

    /** Prints the computed path from intial state to goal state
     * @param bestPath represents the computed path
     */

    public void printBestPath(Stack<Node> bestPath){
        while(bestPath.size() > 0){
            Node currentNode = bestPath.pop();
            printPuzzle(currentNode);
            if(bestPath.size() != 0)
                System.out.println("\t\t|\n\t\t|\n\t\tV");
        }
    }
}
