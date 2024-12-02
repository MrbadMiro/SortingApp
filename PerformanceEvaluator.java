// PerformanceEvaluator.java
import java.util.Arrays;

public class PerformanceEvaluator {
    public static long evaluateSortingPerformance(int[] array, String algorithm) {
        int[] tempArray = Arrays.copyOf(array, array.length);
        long startTime = System.nanoTime();

        switch (algorithm) {
            case "Insertion Sort" -> SortingAlgorithms.insertionSort(tempArray);
            case "Shell Sort" -> SortingAlgorithms.shellSort(tempArray);
            case "Merge Sort" -> SortingAlgorithms.mergeSort(tempArray, 0, tempArray.length - 1);
            case "Quick Sort" -> SortingAlgorithms.quickSort(tempArray, 0, tempArray.length - 1);
            case "Heap Sort" -> SortingAlgorithms.heapSort(tempArray);
            default -> throw new IllegalArgumentException("Unknown sorting algorithm: " + algorithm);
        }

        long endTime = System.nanoTime();
        return endTime - startTime;
    }
}