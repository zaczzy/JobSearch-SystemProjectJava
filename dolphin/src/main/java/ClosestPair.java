import java.util.LinkedList;
import java.util.List;

public class ClosestPair {

    public static int[] findClosestIndices(List<List<Integer>> lists) {
        if (lists.size() < 1) { return null; }
        if (lists.size() == 1) {
            int[] result = {0, lists.get(0).get(0), 200};
            return result;
        } else if (lists.size() == 2) {
            return findClosestIndices(lists.get(0), lists.get(1));
        } else {
            return findClosestIndices(lists.get(0), lists.get(1), lists.get(2));
        }
    }

    // This returns an array [d, I1, I2], where d is the closest difference, and (I1, I2) is the corresponding index pair.
    // If there is no such pair, then the result array is [Inf, 0, 0].
    public static int[] findClosestIndices(List<Integer> firstList, List<Integer> secondList) {
        int[] result1 = findOrderedClosestIndices(firstList, secondList);
        int[] result2 = findOrderedClosestIndices(secondList, firstList);

        if (result1[0] <= result2[0]) {
            return result1;
        } else {
            return result2;
        }
    }
    
    
    private static int[] findOrderedClosestIndices(List<Integer> firstList, List<Integer> secondList) {
        int[] result = new int[3];
        result[0] = Integer.MAX_VALUE;
        if (firstList == null || secondList == null) {
            throw new RuntimeException("Input Lists are null");
        }
        
        int secondPointer = 0;
        int smallestDiff = Integer.MAX_VALUE;
        int secondListLen = secondList.size();
        
        for (int i = 0; i < firstList.size(); i++) {
            while (secondList.get(secondPointer) < firstList.get(i)) {
                secondPointer++;
                if (secondPointer == secondListLen) {
                    return result;
                }
            }
            
            if (secondList.get(secondPointer) - firstList.get(i) < smallestDiff) {
                int a = secondList.get(secondPointer);
                int b = firstList.get(i);
                smallestDiff = a - b;
                result[0] = smallestDiff;
                result[1] = b;
                result[2] = a;
            }
        }
        
        return result;
    }
    
    
    // This returns an array [d, I1, I2, I3], where d is the smallest span that includes three words in order,
    // and (I1, I2, I3) is the corresponding index tuple.
    // If there is no such tuple, then the result array is [Inf, 0, 0, 0].
    public static int[] findClosestIndices(List<Integer> firstList, List<Integer> secondList, List<Integer> thirdList) {
        List<int[]> results = new LinkedList<int[]>();
        results.add(findOrderedClosestIndices(firstList, secondList, thirdList));
        results.add(findOrderedClosestIndices(secondList, firstList, thirdList));
        results.add(findOrderedClosestIndices(firstList, thirdList, secondList));
        results.add(findOrderedClosestIndices(secondList, thirdList, firstList));
        results.add(findOrderedClosestIndices(thirdList, firstList, secondList));
        results.add(findOrderedClosestIndices(thirdList, secondList, firstList));
        
        
        int minGap = Integer.MAX_VALUE;
        int minResults = 0;
        for (int i = 0; i < 6; i++) {
            if (results.get(i)[0] < minGap) {
                minGap = results.get(i)[0];
                minResults = i;
            }
        }
        
        return results.get(minResults);
    }
    
    private static int[] findOrderedClosestIndices(List<Integer> firstList, List<Integer> secondList, List<Integer> thirdList) {
        int[] result = new int[4];
        result[0] = Integer.MAX_VALUE;
        if (firstList == null || secondList == null || thirdList == null) {
            throw new RuntimeException("Input Lists are null");
        }
        
        int secondPointer = 0;
        int thirdPointer = 0;
        int smallestDiff = Integer.MAX_VALUE;
        int secondListLen = secondList.size();
        int thirdListLen = thirdList.size();
        
        for (int i = 0; i < firstList.size(); i++) {
            while (secondList.get(secondPointer) < firstList.get(i)) {
                secondPointer++;
                if (secondPointer == secondListLen) {
                    return result;
                }
            }
            
            while (thirdList.get(thirdPointer) < secondList.get(secondPointer)) {
                thirdPointer++;
                if (thirdPointer == thirdListLen) {
                    return result;
                }
            }
            
            if (thirdList.get(thirdPointer) - firstList.get(i) < smallestDiff) {
                smallestDiff = thirdList.get(thirdPointer) - firstList.get(i);
                result[0] = smallestDiff;
                result[1] = firstList.get(i);
                result[2] = secondList.get(secondPointer);
                result[3] = thirdList.get(thirdPointer);
            }
        }
        
        return result;
    }
}
