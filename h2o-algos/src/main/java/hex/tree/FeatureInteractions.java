package hex.tree;

import org.apache.commons.lang.mutable.MutableInt;
import water.util.TwoDimTable;

import java.util.*;
import java.util.stream.Collectors;

public class FeatureInteractions extends HashMap<String, FeatureInteraction> {

    public static void merge(Map<String, FeatureInteraction> leftFeatureInteractions, Map<String, FeatureInteraction> rightFeatureInteractions) {
        for (Map.Entry<String,FeatureInteraction> currEntry : rightFeatureInteractions.entrySet()) {
            if (leftFeatureInteractions.containsKey(currEntry.getKey())) {
                FeatureInteraction leftFeatureInteraction = leftFeatureInteractions.get(currEntry.getKey());
                FeatureInteraction rightFeatureInteraction = currEntry.getValue();
                leftFeatureInteraction.gain += rightFeatureInteraction.gain;
                leftFeatureInteraction.cover += rightFeatureInteraction.cover;
                leftFeatureInteraction.FScore += rightFeatureInteraction.FScore;
                leftFeatureInteraction.FScoreWeighted += rightFeatureInteraction.FScoreWeighted;
                leftFeatureInteraction.averageFScoreWeighted = leftFeatureInteraction.FScoreWeighted / leftFeatureInteraction.FScore;
                leftFeatureInteraction.averageGain = leftFeatureInteraction.gain / leftFeatureInteraction.FScore;
                leftFeatureInteraction.expectedGain += rightFeatureInteraction.expectedGain;
                leftFeatureInteraction.treeIndex += rightFeatureInteraction.treeIndex;
                leftFeatureInteraction.averageTreeIndex = leftFeatureInteraction.treeIndex / leftFeatureInteraction.FScore;
                leftFeatureInteraction.treeDepth += rightFeatureInteraction.treeDepth;
                leftFeatureInteraction.averageTreeDepth = leftFeatureInteraction.treeDepth / leftFeatureInteraction.FScore;
                leftFeatureInteraction.sumLeafCoversRight += rightFeatureInteraction.sumLeafCoversRight;
                leftFeatureInteraction.sumLeafCoversLeft += rightFeatureInteraction.sumLeafCoversLeft;
                leftFeatureInteraction.sumLeafValuesRight += rightFeatureInteraction.sumLeafValuesRight;
                leftFeatureInteraction.sumLeafValuesLeft += rightFeatureInteraction.sumLeafValuesLeft;
                leftFeatureInteraction.splitValueHistogram.merge(rightFeatureInteraction.splitValueHistogram);
            } else {
                leftFeatureInteractions.put(currEntry.getKey(), currEntry.getValue());
            }
        }
    }

    public int maxDepth() {
        return Collections.max(this.entrySet(), Comparator.comparingInt(entry -> entry.getValue().depth)).getValue().depth;
    }
    
    public TwoDimTable[] getFeatureInteractions() {
        int maxDepth = maxDepth();
        TwoDimTable[] twoDimTables = new TwoDimTable[maxDepth + 1];

        for (int depth = 0; depth < maxDepth + 1; depth++) {
            twoDimTables[depth] = constructFeatureInteractionsTable(depth);
        }
        
        return twoDimTables;
    }
    
    List<FeatureInteraction> getFeatureInteractionsOfDepth(int depthRequired) {
        return this.entrySet()
                .stream()
                .filter(entry -> entry.getValue().depth == depthRequired)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    List<FeatureInteraction> getFeatureInteractionsWithLeafStatistics() {
        return this.entrySet()
                .stream()
                .filter(entry -> entry.getValue().hasLeafStatistics == true)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
    
    TwoDimTable constructFeatureInteractionsTable(int depth) {
        String[] colHeaders = new String[] {"Interaction", "Gain", "FScore", "wFScore", "Average wFScore", "Average Gain", 
                "Expected Gain", "Gain Rank", "FScore Rank", "wFScore Rank", "Avg wFScore Rank", "Avg Gain Rank", 
                "Expected Gain Rank", "Average Rank", "Average Tree Index", "Average Tree Depth"};
        String[] colTypes = new String[] {"string", "double", "double", "double", "double", "double",
                "double", "int", "int", "int", "int", "int",
                "int", "double", "double", "double"};
        String[] colFormat = new String[] {"%s", "%.5f", "%.5f", "%.5f", "%.5f", "%.5f",
                "%.5f", "%d", "%d", "%d", "%d", "%d",
                "%d", "%.5f", "%.5f", "%.5f"};

        List<FeatureInteraction> featureInteractions = getFeatureInteractionsOfDepth(depth);
        int numRows = featureInteractions.size();

        List<FeatureInteraction> gainSorted = new ArrayList(featureInteractions);
        gainSorted.sort(Comparator.comparing(entry -> -entry.gain));

        List<FeatureInteraction> fScoreSorted = new ArrayList(featureInteractions);
        fScoreSorted.sort(Comparator.comparing(entry -> -entry.FScore));

        List<FeatureInteraction> fScoreWeightedSorted = new ArrayList(featureInteractions);
        fScoreWeightedSorted.sort(Comparator.comparing(entry -> -entry.FScoreWeighted));

        List<FeatureInteraction> averagefScoreWeightedSorted = new ArrayList(featureInteractions);
        averagefScoreWeightedSorted.sort(Comparator.comparing(entry -> -entry.averageFScoreWeighted));

        List<FeatureInteraction> averageGainSorted = new ArrayList(featureInteractions);
        averageGainSorted.sort(Comparator.comparing(entry -> -entry.averageGain));

        List<FeatureInteraction> expectedGainSorted = new ArrayList(featureInteractions);
        expectedGainSorted.sort(Comparator.comparing(entry -> -entry.expectedGain));
        

        TwoDimTable table = new TwoDimTable(
          "Interaction Depth " + depth, null,
          new String[numRows],
          colHeaders,
          colTypes,
          colFormat,
          "");
        
        for (int i = 0; i < numRows; i++) {
            String name = featureInteractions.get(i).name;
            table.set(i, 0, name);
            table.set(i, 1, featureInteractions.get(i).gain);
            table.set(i, 2, featureInteractions.get(i).FScore);
            table.set(i, 3, featureInteractions.get(i).FScoreWeighted);
            table.set(i, 4, featureInteractions.get(i).averageFScoreWeighted);
            table.set(i, 5, featureInteractions.get(i).averageGain);
            table.set(i, 6, featureInteractions.get(i).expectedGain);
            double gainRank = indexOfInteractionWithName(name, gainSorted) + 1;
            table.set(i, 7, gainRank);
            double FScoreRank = indexOfInteractionWithName(name, fScoreSorted) + 1;
            table.set(i, 8, FScoreRank);
            double FScoreWeightedRank = indexOfInteractionWithName(name, fScoreWeightedSorted) + 1;
            table.set(i, 9, FScoreWeightedRank);
            double avgFScoreWeightedRank = indexOfInteractionWithName(name, averagefScoreWeightedSorted) + 1;
            table.set(i, 10, avgFScoreWeightedRank);
            double averageGain = indexOfInteractionWithName(name, averageGainSorted) + 1;
            table.set(i, 11, averageGain);
            double expectedGain = indexOfInteractionWithName(name, expectedGainSorted) + 1;
            table.set(i, 12, expectedGain);
            table.set(i, 13, (gainRank + FScoreRank + FScoreWeightedRank + avgFScoreWeightedRank + averageGain + expectedGain) / 6);
            table.set(i, 14, featureInteractions.get(i).averageTreeIndex);
            table.set(i, 15, featureInteractions.get(i).averageTreeDepth);
        }
        
        return table;
    }


    int indexOfInteractionWithName(String name, List<FeatureInteraction> featureInteractions) {
        for (int i = 0; i < featureInteractions.size(); i++)
            if (featureInteractions.get(i).name == name)
                return i;
        
        return -1;
    }
    

    public TwoDimTable getLeafStatisticsTable() {
        String[] colHeaders = new String[] {"Interaction", "Sum Leaf Values Left", "Sum Leaf Values Right", "Sum Leaf Covers Left", "Sum Leaf Covers Right"};
        String[] colTypes = new String[] {"string", "double", "double", "double", "double"};
        String[] colFormat = new String[] {"%s", "%.5f", "%.5f", "%.5f", "%.5f"};

        List<FeatureInteraction> featureInteractions = getFeatureInteractionsWithLeafStatistics();
        int numRows = featureInteractions.size();

        TwoDimTable table = new TwoDimTable(
                "Leaf Statistics", null,
                new String[numRows],
                colHeaders,
                colTypes,
                colFormat,
                "");

        for (int i = 0; i < numRows; i++) {
            table.set(i, 0, featureInteractions.get(i).name);
            table.set(i, 1, featureInteractions.get(i).sumLeafValuesLeft);
            table.set(i, 2, featureInteractions.get(i).sumLeafValuesRight);
            table.set(i, 3, featureInteractions.get(i).sumLeafCoversLeft);
            table.set(i, 4, featureInteractions.get(i).sumLeafCoversRight);
        }
        
        return table;
    }
    
    public TwoDimTable[] getSplitValueHistograms() {
        List<FeatureInteraction> featureInteractions = getFeatureInteractionsOfDepth(0);
        int numHistograms = featureInteractions.size();
        
        TwoDimTable[] splitValueHistograms = new TwoDimTable[numHistograms];
        
        for (int i = 0; i < numHistograms; i++) {
            splitValueHistograms[i] = constructHistogramForFeatureInteraction(featureInteractions.get(i));
        }
        
        return splitValueHistograms;
    }
    
    TwoDimTable constructHistogramForFeatureInteraction(FeatureInteraction featureInteraction) {
        String[] colHeaders = new String[] {"Split Value", "Count"};
        String[] colTypes = new String[] {"double", "int"};
        String[] colFormat = new String[] {"%.5f", "%d"};
        
        int N = featureInteraction.splitValueHistogram.entrySet().size();

        TwoDimTable table = new TwoDimTable(
                featureInteraction.name + " Split Value Histogram", null,
                new String[N],
                colHeaders,
                colTypes,
                colFormat,
                "");
        int i = 0;
        for (Map.Entry<Double, MutableInt> entry : featureInteraction.splitValueHistogram.entrySet()) {
            table.set(i, 0, entry.getKey());
            table.set(i, 1, entry.getValue().intValue());
            i++;
        }
        
        return table;
    }
    
}