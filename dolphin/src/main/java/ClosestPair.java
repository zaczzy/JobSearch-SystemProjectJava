import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ClosestPair {

    public static int[] findClosestIndices(List<List<Integer>> list) {
        if (list.size() < 2) {
            System.out.println("Given only 1");
            return null;
        }
        if (list.size() == 2) {
            return findClosestIndices(list.get(0), list.get(1));
        } else {
            return findClosestIndices(list.get(0), list.get(1), list.get(2));
        }
    }

    // This returns an array [d, I1, I2], where d is the closest difference, and (I1, I2) is the corresponding index pair.
    // If there is no such pair, then the result array is [0, 0, 0].
    public static int[] findClosestIndices(List<Integer> firstList, List<Integer> secondList) {
        int[] result = new int[3];
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
                smallestDiff = secondList.get(secondPointer) - firstList.get(i);
                result[0] = smallestDiff;
                result[1] = i;
                result[2] = secondPointer;
            }
        }
        
        return result;
    }
    
    
    // This returns an array [d, I1, I2, I3], where d is the smallest span that includes three words in order,
    // and (I1, I2, I3) is the corresponding index tuple.
    // If there is no such tuple, then the result array is [0, 0, 0, 0].
    public static int[] findClosestIndices(List<Integer> firstList, List<Integer> secondList, List<Integer> thirdList) {
        int[] result = new int[4];
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
                result[1] = i;
                result[2] = secondPointer;
                result[3] = thirdPointer;
            }
        }
        
        return result;
    }
}
