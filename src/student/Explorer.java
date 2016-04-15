package student;

import game.EscapeState;
import game.ExplorationState;
import game.Node;
import game.NodeStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;


public class Explorer {
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int FIVE_HUNDRED = 500;
    private Map<Node, Integer> distanceMap;

    /**
     * Explore the cavern, trying to find the orb in as few steps as possible.
     * Once you find the orb, you must return from the function in order to pick
     * it up. If you continue to move after finding the orb rather
     * than returning, it will not count.
     * If you return from this function while not standing on top of the orb,
     * it will count as a failure.
     * <p>
     * There is no limit to how many steps you can take, but you will receive
     * a score bonus multiplier for finding the orb in fewer steps.
     * <p>
     * At every step, you only know your current tile's ID and the ID of all
     * open neighbor tiles, as well as the distance to the orb at each of these tiles
     * (ignoring walls and obstacles).
     * <p>
     * To get information about the current state, use functions
     * getCurrentLocation(),
     * getNeighbours(), and
     * getDistanceToTarget()
     * in ExplorationState.
     * You know you are standing on the orb when getDistanceToTarget() is 0.
     * <p>
     * Use function moveTo(long id) in ExplorationState to move to a neighboring
     * tile by its ID. Doing this will change state to reflect your new position.
     * <p>
     * A suggested first implementation that will always find the orb, but likely won't
     * receive a large bonus multiplier, is a depth-first search.
     *
     * @param state the information available at the current state
     */
    public void explore(ExplorationState state) {

        Set<Long> seen = new LinkedHashSet<>(); //a set to store nodes that have already been visited
        Stack<Long> dfs = new Stack<>(); //stack to use for the depth first search
        Stack<Long> retraceSteps = new Stack<>(); //stack to retrace steps
        //add the only possible starting node onto the stack
        Collection<NodeStatus> collectionNodeStatus = state.getNeighbours();
        collectionNodeStatus.stream().forEach((s) -> dfs.push(s.getId()));
        seen.add(state.getCurrentLocation());
        //now enter loop to keep moving until the orb is reached
        while (!dfs.isEmpty()) {
            collectionNodeStatus = state.getNeighbours();
            //make sure that the next node to move to is adjacent
            if (collectionNodeStatus.stream().anyMatch((s) -> s.getId() == dfs.peek())) {
                retraceSteps.push(state.getCurrentLocation());
                state.moveTo(dfs.pop());
            } else {
                //if not adjacent then need to retrace steps until it is adjacent
                while (!collectionNodeStatus.stream().anyMatch((s) -> s.getId() == dfs.peek())) {
                    state.moveTo(retraceSteps.pop());
                    collectionNodeStatus = state.getNeighbours();
                }
                retraceSteps.push(state.getCurrentLocation());
                state.moveTo(dfs.pop());
            }
            if (!seen.contains(state.getCurrentLocation())) {
                seen.add(state.getCurrentLocation());
            }
            if (state.getDistanceToTarget() == ZERO) {
                break;
            } else {
                collectionNodeStatus = state.getNeighbours();
                //first add those not seen that are equal or further away to the orb
                collectionNodeStatus.stream().filter((s) -> !seen.contains(s.getId()))
                        .filter((s) -> s.getDistanceToTarget() >= state.getDistanceToTarget())
                        .forEach((s) -> dfs.push(s.getId()));
                //then add those not seen that are closer to the orb so these will be looked at first
                collectionNodeStatus.stream().filter((s) -> !seen.contains(s.getId()))
                        .filter((s) -> s.getDistanceToTarget() < state.getDistanceToTarget())
                        .forEach((s) -> dfs.push(s.getId()));
            }
        }

    }


    /**
     * Escape from the cavern before the ceiling collapses, trying to collect as much
     * gold as possible along the way. Your solution must ALWAYS escape before time runs
     * out, and this should be prioritized above collecting gold.
     * <p>
     * You now have access to the entire underlying graph, which can be accessed through EscapeState.
     * getCurrentNode() and getExit() will return you Node objects of interest, and getVertices()
     * will return a collection of all nodes on the graph.
     * <p>
     * Note that time is measured entirely in the number of steps taken, and for each step
     * the time remaining is decremented by the weight of the edge taken. You can use
     * getTimeRemaining() to get the time still remaining, pickUpGold() to pick up any gold
     * on your current tile (this will fail if no such gold exists), and moveTo() to move
     * to a destination node adjacent to your current node.
     * <p>
     * You must return from this function while standing at the exit. Failing to do so before time
     * runs out or returning from the wrong location will be considered a failed run.
     * <p>
     * You will always have enough time to escape using the shortest path from the starting
     * position to the exit, although this will not collect much gold.
     *
     * @param state the information available at the current state
     */
    public void escape(EscapeState state) {
        int timeForNextMove = ZERO;
        int max = ZERO; // used to store the value of the largest pile of gold available


        Collection<Node> map = state.getVertices();
        List<Node> myMap = new ArrayList<>(); //CHANGE NAME OF MYMAP!!!
        map.stream().forEach(myMap::add);
        myMap.sort((x, y) -> ((Integer)y.getTile().getGold()).compareTo(((Integer)x.getTile().getGold())));
        //myMap is now a list of all nodes in order of amount of gold on each node

        routeToNode(state.getCurrentNode(), state.getExit());

        // this is my "searching for gold" loop - i.e. there is surplus time so look for more gold!
        while (state.getTimeRemaining() > timeForNextMove) {

            // pick up any gold on current node
            if (state.getCurrentNode().getTile().getGold() > ZERO) {
                state.pickUpGold();
            }


            // this section is to find the largest pile of available gold in the cavern
            // that there is enough time to reach
            Stack<Node> findGold = new Stack<>(); // stack for gold finding
            myMap.remove(state.getCurrentNode());

            /*
            Node maxGold = null;
            while (maxGold == null && myMap.size() > ZERO) {
                findGold.clear();
                maxGold = myMap.get(ZERO); // first in list has the most gold as this was sorted earlier
                max = maxGold.getTile().getGold();
                findGold = routeToNode(state.getCurrentNode(), maxGold); // finds the route to maxGold and stores it on the findGold stack

                // check if there is enough time to get to gold and then to exit
                if (timeToNode(maxGold, state.getExit()) + timeToNode(state.getCurrentNode(), maxGold) > state.getTimeRemaining()) {
                    // node is too far away so remove it from myMap and repeat loop with the next largest
                    maxGold = null;
                    myMap.remove(ZERO);
                }
            }
            */

            myMap.sort((x, y) -> distanceMap.get(x).compareTo(distanceMap.get(y)));

            while (!myMap.isEmpty() && myMap.get(ZERO).getTile().getGold() == ZERO) {
                myMap.remove(ZERO);
            }
            if (myMap.isEmpty()) {
                break;
            }

            findGold = routeToNode(state.getCurrentNode(), myMap.get(ZERO));
            max = myMap.get(ZERO).getTile().getGold();


            // if max == 0 then there is no gold within reach so head straight for exit
            if (max == ZERO) {
                break;
            }

            /*
            //check neighbouring tiles for gold above 500 and add to findGold stack if there are any
            Collection<Node> neighbours = state.getCurrentNode().getNeighbours();
            for (Node n : neighbours) {
                if (n.getTile().getGold() > FIVE_HUNDRED && n.getTile().getGold() > findGold.peek().getTile().getGold()) {
                    findGold.push(n);
                    break;
                }
            }
            */

            timeForNextMove = timeToNode(findGold.peek(), state.getExit()) + state.getCurrentNode().getEdge(findGold.peek()).length();

            if (timeForNextMove <= state.getTimeRemaining()) {
                //enough time to make move
                state.moveTo(findGold.pop());
            } else {
                //not enough time and need to head for exit
                break;
            }
        }



        // this is my "head for the exit!" loop - i.e. there is only enough time to exit

        Stack<Node> escapeRoute = routeToNode(state.getCurrentNode(), state.getExit());

        while (state.getCurrentNode() != state.getExit()) {

            //check neighbouring tiles for gold and if there is enough time visit them and pick up gold
            Collection<Node> neighbours = state.getCurrentNode().getNeighbours();
            for (Node n : neighbours) {
                if (n.getTile().getGold() > ZERO && !n.equals(escapeRoute.peek())) {
                    if (timeToNode(n, state.getExit()) + state.getCurrentNode().getEdge(n).length() <= state.getTimeRemaining()) {
                        state.moveTo(n);
                        state.pickUpGold();
                        escapeRoute = routeToNode(state.getCurrentNode(), state.getExit());
                        break;
                    }
                }
            }

            // pick up any gold on current tile
            if (state.getCurrentNode().getTile().getGold() > ZERO) {
                state.pickUpGold();
            }

            // move to next tile on escape route
            state.moveTo(escapeRoute.pop());
        }
    }

    private Stack<Node> routeToNode(Node nodeA, Node nodeB) {
        Stack<Node> route = new Stack<>();
        Queue<Node> planRoute = new ArrayDeque<>(); // queue for the BFS
        Map<Node, Node> parentMap = new HashMap<>(); // map to link nodes to parent nodes for route finding
        distanceMap = new HashMap<>();

        distanceMap.put(nodeA, ZERO);
        parentMap.put(nodeA, null); // add current node to map with no parent node
        planRoute.add(nodeA); // add current node to BFS queue

        while(!planRoute.isEmpty()) {
            // take a node from the BFS queue and look at all it's neighbouring nodes
            Node current = planRoute.poll();
            Collection<Node> cns = current.getNeighbours();
            // if they have not been looked at add them to the parent map and BFS queue
            cns.stream().filter((s) -> !parentMap.containsKey(s)).forEach((s) -> {
                parentMap.put(s, current);
                distanceMap.put(s, distanceMap.get(current) + 1);
                planRoute.add(s);
            });
        }

        // push whole route onto stack

        while (nodeB != nodeA) {
            route.push(nodeB);
            nodeB = parentMap.get(nodeB);
        }

        return route;


    }

    private int timeToNode (Node nodeA, Node nodeB) {
        Stack<Node> route = routeToNode(nodeA, nodeB);
        route.push(nodeA);
        int time = ZERO;
        for (int i = ZERO; i < route.size() - ONE; i++) {
            time += route.get(i).getEdge(route.get(i + ONE)).length();
        }
        return time;
    }
}