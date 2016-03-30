package student;

import game.EscapeState;
import game.ExplorationState;
import game.Node;
import game.NodeStatus;

import java.util.*;

public class Explorer {

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
            if (state.getDistanceToTarget() == 0) {
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
        Stack<Node> escapeRoute = new Stack<>();
        Queue<Node> planRoute = new ArrayDeque<>();
        Map<Node, Integer> distanceMap = new HashMap<>();
        Map<Node, Node> parentMap = new HashMap<>();
        Collection<Node> map = state.getVertices();


        distanceMap.put(state.getCurrentNode(), 0);
        parentMap.put(state.getCurrentNode(), null);
        planRoute.add(state.getCurrentNode());

        while(!planRoute.isEmpty()) {
            Node current = planRoute.poll();
            Collection<Node> cns = current.getNeighbours();
            for (Node n : cns) {
                if (!distanceMap.containsKey(n)) {
                    distanceMap.put(n, distanceMap.get(current) + 1);
                    parentMap.put(n, current);
                    planRoute.add(n);
                }
            }
        }

        Node current = state.getExit();
        while (current != state.getCurrentNode()) {
            escapeRoute.push(current);
            current = parentMap.get(current);
        }

        System.out.println("Distance to exit: " + distanceMap.get(state.getExit()));
        System.out.println("Distance of route taken: " + escapeRoute.size());

        while (state.getCurrentNode() != state.getExit()) {
            if (state.getCurrentNode().getTile().getGold() > 0) {
                state.pickUpGold();
            }
            state.moveTo(escapeRoute.pop());
        }




    }
}
