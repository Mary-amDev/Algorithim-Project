
import java.util.*;

public class Algorithms {

    static Long timeA, timeB, timeC, timeD;
    static int weights[], values[];
    static int Capacity = 30;
    static int items, trial;
    static Item itemsArr[];

    public static void main(String[] args) {
        
        for (items = 5; items <= 20; items++) {
            
            System.out.println("\n\tNumber of items " + items);

             for (trial = 1; trial <= 20; trial++) {

                fillArray();
                
                System.out.print("");
                
                timeA = System.nanoTime();
                BruteForce();
                timeB = System.nanoTime();
                Tabular(Capacity, weights, values, items);
                timeC = System.nanoTime();
                BranchAndBound(itemsArr);
                timeD = System.nanoTime();
                System.out.printf("  trail No.%-5d BruteForce %-10d Tabular %-10d BranchAndBound %-10d %n", trial, (timeB - timeA), (timeC - timeB), (timeD - timeC));

            }

        }

    }

    static void fillArray() {
        values = weights = new int[items];
        itemsArr = new Item[items];
        for (int i = 0; i < items; i++) {
            values[i] = (int) ((Math.random() * 500) + 500);
            weights[i] = (int) ((Math.random() * 29) + 1);
            itemsArr[i] = new Item(values[i], weights[i], i);
        }
    }

    static void BruteForce() {
        
        int bestW = 0;
        int bestV = 0;
        int[] Knapsack = new int[items];
        
        for (int i = 0; i < Math.pow(2, items); i++) {

            int tempW = 0;
            int tempV = 0;

            

            for (int j = 0; j < Knapsack.length; j++) {
                Knapsack[j] = 0;
            }
            Knapsack[0] = 1;

            for (int k = 0; k < items; k++) {
                if (Knapsack[k] == 1) {
                    tempW += weights[k];
                    tempV += values[k];
                }
            }

            if ((tempV > bestV) && (tempW <= bestW)) {
                bestV = tempV;
                bestW = tempW;
            }

        }

    }

    static int Tabular(int W, int wt[], int val[], int n) {

        int K[][] = new int[n + 1][W + 1];
        for (int i = 0; i <= n; i++) {

            for (int w = 0; w <= W; w++) {

                if (i == 0 || w == 0) {
                    K[i][w] = 0;
                } else if (wt[i - 1] <= w) {
                    K[i][w] = Math.max(val[i - 1] + K[i - 1][w - wt[i - 1]], K[i - 1][w]);
                } else {
                    K[i][w] = K[i - 1][w];
                }
            }
        }
        
        return K[n][W];
    }

    // Function to calculate upper bound
    // (includes fractional part of the items)
    static float upperBound(float tv, float tw, int idx, Item arr[]) {
        float value = tv;
        float weight = tw;
        for (int i = idx; i < items; i++) {
            if (weight + arr[i].weight
                    <= Capacity) {
                weight += arr[i].weight;
                value -= arr[i].value;
            } else {
                value -= (float) (Capacity
                        - weight)
                        / arr[i].weight
                        * arr[i].value;
                break;
            }
        }
        return value;
    }

    // Calculate lower bound (doesn't
    // include fractional part of items)
    static float lowerBound(float tv, float tw, int idx, Item arr[]) {
        float value = tv;
        float weight = tw;
        for (int i = idx; i < items; i++) {
            if (weight + arr[i].weight <= Capacity) {
                weight += arr[i].weight;
                value -= arr[i].value;
            } else {
                break;
            }
        }
        return value;
    }

    static void assign(Node a, float ub, float lb, int level, boolean flag, float tv, float tw) {
        a.ub = ub;
        a.lb = lb;
        a.level = level;
        a.flag = flag;
        a.tv = tv;
        a.tw = tw;
    }

    static void BranchAndBound(Item arr[]) {
        // Sort the items based on the
        // profit/weight ratio
        Arrays.sort(arr, new sortByRatio());

        Node current, left, right;
        current = new Node();
        left = new Node();
        right = new Node();

        // min_lb -> Minimum lower bound
        // of all the nodes explored
        // final_lb -> Minimum lower bound
        // of all the paths that reached
        // the final level
        float minLB = 0, finalLB = Integer.MAX_VALUE;
        current.tv = current.tw = current.ub = current.lb = 0;
        current.level = 0;
        current.flag = false;

        // Priority queue to store elements
        // based on lower bounds
        PriorityQueue<Node> pq = new PriorityQueue<>(new sortByC());

        // Insert a dummy node
        pq.add(current);

        // curr_path -> Boolean array to store
        // at every index if the element is
        // included or not
        // final_path -> Boolean array to store
        // the result of selection array when
        // it reached the last level
        boolean currPath[] = new boolean[items];
        boolean finalPath[] = new boolean[items];

        while (!pq.isEmpty()) {
            current = pq.poll();
            if (current.ub > minLB
                    || current.ub >= finalLB) {
                // if the current node's best case
                // value is not optimal than minLB,
                // then there is no reason to
                // explore that node. Including
                // finalLB eliminates all those
                // paths whose best values is equal
                // to the finalLB
                continue;
            }

            if (current.level != 0) {
                currPath[current.level - 1]
                        = current.flag;
            }

            if (current.level == items) {
                if (current.lb < finalLB) {
                    // Reached last level
                    for (int i = 0; i < items; i++) {
                        finalPath[arr[i].idx]
                                = currPath[i];
                    }
                    finalLB = current.lb;
                }
                continue;
            }

            int level = current.level;

            // right node -> Exludes current item
            // Hence, cp, cw will obtain the value
            // of that of parent
            assign(right, upperBound(current.tv,
                    current.tw,
                    level + 1, arr),
                    lowerBound(current.tv, current.tw,
                            level + 1, arr),
                    level + 1, false,
                    current.tv, current.tw);

            if (current.tw + arr[current.level].weight <= Capacity) {

                // left node -> includes current item
                // c and lb should be calculated
                // including the current item.
                left.ub = upperBound(
                        current.tv
                        - arr[level].value,
                        current.tw
                        + arr[level].weight,
                        level + 1, arr);
                left.lb = lowerBound(
                        current.tv
                        - arr[level].value,
                        current.tw
                        + arr[level].weight,
                        level + 1,
                        arr);
                assign(left, left.ub, left.lb,
                        level + 1, true,
                        current.tv - arr[level].value,
                        current.tw
                        + arr[level].weight);
            } // If the left node cannot
            // be inserted
            else {

                // Stop the left node from
                // getting added to the
                // priority queue
                left.ub = left.lb = 1;
            }

            // Update minLB
            minLB = Math.min(minLB, left.lb);
            minLB = Math.min(minLB, right.lb);

            if (minLB >= left.ub) {
                pq.add(new Node(left));
            }
            if (minLB >= right.ub) {
                pq.add(new Node(right));
            }
        }

    }
}

class Item {

    // Stores the weight
    // of items
    float weight;

    // Stores the values
    // of items
    int value;

    // Stores the index
    // of items
    int idx;

    public Item() {
    }

    public Item(int value, float weight, int idx) {
        this.value = value;
        this.weight = weight;
        this.idx = idx;
    }
}

class Node {

    // Upper Bound: Best case
    // (Fractional Knapsack)
    float ub;

    // Lower Bound: Worst case
    // (0/1)
    float lb;

    // Level of the node in
    // the decision tree
    int level;

    // Stores if the current
    // item is selected or not
    boolean flag;

    // Total Value: Stores the
    // sum of the values of the
    // items included
    float tv;

    // Total Weight: Stores the sum of
    // the weights of included items
    float tw;

    public Node() {
    }

    public Node(Node cpy) {
        this.tv = cpy.tv;
        this.tw = cpy.tw;
        this.ub = cpy.ub;
        this.lb = cpy.lb;
        this.level = cpy.level;
        this.flag = cpy.flag;
    }
}

// Comparator to sort based on lower bound
class sortByC implements Comparator<Node> {

    @Override
    public int compare(Node a, Node b) {
        boolean temp = a.lb > b.lb;
        return temp ? 1 : -1;
    }
}

class sortByRatio implements Comparator<Item> {

    @Override
    public int compare(Item a, Item b) {
        boolean temp = (float) a.value
                / a.weight
                > (float) b.value
                / b.weight;
        return temp ? -1 : 1;
    }
}
