package com.kmlsiuzdak;

import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public class generate_service {

    private final int xSize;
    private final int ySize;
    private final int cells;
    private final Border_transition selection;
    private final int inclusions;
    private final int inclusionLength;
    private final int probability;
    private final Inclusion_border inclusionborder;
    private final Inclusion_shape inclusionShape;
    private final int[][] matrix;
    private final Map<Integer, Color> colorMap;
    private final PixelWriter pixelWriter;
    private List<String> selectedGrainsForStructure;
    private Structure structure;

    public generate_service(int xSize, int ySize, int cells, Border_transition selection, int inclusions,
                            int inclusionLength, int probability, Inclusion_border inclusionborder,
                            Inclusion_shape inclusionShape, int[][] matrix, Map<Integer, Color> colorMap, PixelWriter pixelWriter) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.cells = cells;
        this.selection = selection;
        this.inclusions = inclusions;
        this.inclusionLength = inclusionLength;
        this.inclusionborder = inclusionborder;
        this.inclusionShape = inclusionShape;
        this.matrix = matrix;
        this.colorMap = colorMap;
        this.pixelWriter = pixelWriter;
        this.probability = probability;
    }

    public void generate(boolean isShapeControl) {
        if (inclusionborder != null && inclusionShape != null && inclusions != 0) {
            generateWithInclusions(isShapeControl);
        } else {
            defaultGeneration(isShapeControl);
        }
    }

    private void generateWithInclusions(boolean isShapeControl) {
        switch (inclusionborder) {
            case NO:
                generateForBeforeTime(isShapeControl);
                break;
            case YES:
                generateForAfterTime(isShapeControl);
                break;
        }
    }

    private void generateForBeforeTime(boolean isShapeControl) {
        setInclusions();
        setCells();
        growGrains(isShapeControl);
    }

    private void generateForAfterTime(boolean isShapeControl) {
        setCells();
        growGrains(isShapeControl);
        setInclusions();
    }

    private void defaultGeneration(boolean isShapeControl) {
        setCells();
        growGrains(isShapeControl);
    }

    private void setCells() {
        for (int i = 0; i < cells; i++) {
            int x = (int) Math.floor(Math.random() * xSize);
            int y = (int) Math.floor(Math.random() * ySize);
            boolean set = false;
            while (!set) {
                if (matrix[x][y] == 0) {
                    matrix[x][y] = i + 1;
                    pixelWriter.setColor(x, y, colorMap.get(i + 1));
                    set = true;
                }
                x = (int) Math.floor(Math.random() * xSize);
                y = (int) Math.floor(Math.random() * ySize);
            }
        }
    }

    public void setCellsWithIgnoredGrains(List<String> selectedGrainsForStructure) {
        for (int i = 1; i < cells + 1; i++) {
            if (!selectedGrainsForStructure.contains(String.valueOf(i))) {
                int x = (int) Math.floor(Math.random() * xSize);
                int y = (int) Math.floor(Math.random() * ySize);
                boolean set = false;
                while (!set) {
                    if (matrix[x][y] == 0) {
                        matrix[x][y] = i;
                        pixelWriter.setColor(x, y, colorMap.get(i));
                        set = true;
                    }
                    x = (int) Math.floor(Math.random() * xSize);
                    y = (int) Math.floor(Math.random() * ySize);
                }
            }
        }
    }

    private void setInclusions() {
        switch (inclusionborder) {
            case YES:
                setAfterInclusions();
                break;
            case NO:
                setBeforeInclusions();
                break;
        }
    }

    private void setAfterInclusions() {
        List<data_value> datavalues = getBorderElements();
        Random random = new Random();
        for (int i = 0; i < inclusions; i++) {
            boolean set = false;
            while (!set) {
                int randomIndex = random.nextInt(datavalues.size());
                data_value datavalue = datavalues.get(randomIndex);
                if (matrix[datavalue.getX()][datavalue.getY()] != -1) {
                    setInclusion(datavalue.getX(), datavalue.getY());
                    datavalues.remove(randomIndex);
                    set = true;
                }
            }
        }
    }

    private List<data_value> getBorderElements() {
        List<data_value> datavalues = new LinkedList<>();
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (i + 1 != xSize && matrix[i][j] != matrix[i + 1][j]) {
                    datavalues.add(new data_value(i, j, matrix[i][j]));
                }
                if (i - 1 >= 0 && matrix[i][j] != matrix[i - 1][j]) {
                    datavalues.add(new data_value(i, j, matrix[i][j]));
                }
                if (j + 1 != ySize && matrix[i][j] != matrix[i][j + 1]) {
                    datavalues.add(new data_value(i, j, matrix[i][j]));
                }
                if (j - 1 >= 0 && matrix[i][j] != matrix[i][j - 1]) {
                    datavalues.add(new data_value(i, j, matrix[i][j]));
                }
            }
        }
        return datavalues;
    }

    private void setBeforeInclusions() {
        for (int i = 0; i < inclusions; i++) {
            int x = (int) Math.floor(Math.random() * xSize);
            int y = (int) Math.floor(Math.random() * ySize);
            boolean set = false;
            while (!set) {
                if (matrix[x][y] != -1) {
                    setInclusion(x, y);
                    set = true;
                }
                x = (int) Math.floor(Math.random() * xSize);
                y = (int) Math.floor(Math.random() * ySize);
            }
        }
    }

    private void setInclusion(int x, int y) {
        if (inclusionShape.equals(Inclusion_shape.SQUARE)) {
            setSquareInclusion(x, y);
        } else if (inclusionShape.equals(Inclusion_shape.CIRCLE)) {
            setCircleInclusion(x, y);
        }
    }

    private void setCircleInclusion(int x, int y) {
        int numberOfColumns = 0;
        if (inclusionLength >= 3) {
            numberOfColumns = inclusionLength / 3;
        }
        for (int i = inclusionLength; i >= 0; i--) {
            generateShapeBasedOnIndexes(x, y, i, numberOfColumns);
            if (numberOfColumns != inclusionLength) {
                numberOfColumns++;
            }
        }
    }

    private void setSquareInclusion(int x, int y) {
        for (int i = 1; i < inclusionLength + 1; i++) {
            generateShapeBasedOnIndexes(x, y, i, i);
        }
    }

    private void generateShapeBasedOnIndexes(int x, int y, int index, int numberOfColumns) {
        if (x - index >= 0) {
            for (int j = index; j >= 0; j--) {
                for (int k = numberOfColumns; k >= 0; k--) {
                    if (y - k >= 0) {
                        matrix[x - j][y - k] = -1;
                        pixelWriter.setColor(x - j, y - k, colorMap.get(-1));
                    }
                    if (y + k < ySize) {
                        matrix[x - j][y + k] = -1;
                        pixelWriter.setColor(x - j, y + k, colorMap.get(-1));
                    }
                }
            }
        }
        if (x + index < xSize) {
            for (int j = index; j >= 0; j--) {
                for (int k = numberOfColumns; k >= 0; k--) {
                    if (y - k >= 0) {
                        matrix[x + j][y - k] = -1;
                        pixelWriter.setColor(x + j, y - k, colorMap.get(-1));
                    }
                    if (y + k < ySize) {
                        matrix[x + j][y + k] = -1;
                        pixelWriter.setColor(x + j, y + k, colorMap.get(-1));
                    }
                }
            }
        }
    }

    public void growGrains(boolean isShapeControl) {
        int set = 0;
        int stopGrowingCondition = getStopGrowingCondition();
        while (set != stopGrowingCondition) {
            List<data_value> changeList = new LinkedList<>();
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    int number;
                    if (isShapeControl) {
                        number = getShapeControlNumberToSet(matrix[i][j], i, j);
                    } else {
                        number = getNumberToSet(matrix[i][j], i, j);
                    }
                    if (number != 0) {
                        changeList.add(new data_value(i, j, number));
                        set++;
                    }
                }
            }
            setIndexElements(changeList);
        }
    }

    public void growStructureGrains(List<String> selectedGrainsForStructure, Structure structure) {
        this.selectedGrainsForStructure = selectedGrainsForStructure;
        this.structure = structure;
        int set = 0;
        int stopGrowingCondition = getStopGrowingCondition();
        while (set != stopGrowingCondition) {
            List<data_value> changeList = new LinkedList<>();
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    int number = getNumberToSet(matrix[i][j], i, j);
                    if (number != 0) {
                        changeList.add(new data_value(i, j, number));
                        set++;
                    }
                }
            }
            setIndexElements(changeList);
        }
        if (Structure.Substructure.equals(structure)) {
            setBorders();
        }
        this.selectedGrainsForStructure = null;
    }

    private void setBorders() {
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (matrix[i][j] == -2) {
                    int left = 0;
                    int right = 0;
                    int up = 0;
                    int down = 0;
                    if (i + 1 != xSize && matrix[i][j] != matrix[i + 1][j]) {
                        up = matrix[i + 1][j];
                    }
                    if (i - 1 >= 0 && matrix[i][j] != matrix[i - 1][j]) {
                        down = matrix[i - 1][j];
                    }
                    if (j + 1 != ySize && matrix[i][j] != matrix[i][j + 1]) {
                        right = matrix[i][j + 1];
                    }
                    if (j - 1 >= 0 && matrix[i][j] != matrix[i][j - 1]) {
                        left = matrix[i][j - 1];
                    }
                    if (left != 0 && right == 0) {
                        pixelWriter.setColor(i, j, colorMap.get(left));
                    } else if (left == 0 && right != 0) {
                        pixelWriter.setColor(i, j, colorMap.get(right));
                    } else if (up != 0 && down == 0) {
                        pixelWriter.setColor(i, j, colorMap.get(up));
                    } else if (up == 0 && down != 0) {
                        pixelWriter.setColor(i, j, colorMap.get(down));
                    } else {
                        if (left != 0) {
                            pixelWriter.setColor(i, j, colorMap.get(left));
                        }
                        if (right != 0) {
                            pixelWriter.setColor(i, j, colorMap.get(right));
                        }
                        if (up != 0) {
                            pixelWriter.setColor(i, j, colorMap.get(up));
                        }
                        if (down != 0) {
                            pixelWriter.setColor(i, j, colorMap.get(down));
                        }
                    }
                }
            }
        }
    }

    private void setIndexElements(List<data_value> changeList) {
        for (data_value datavalue : changeList) {
            matrix[datavalue.getX()][datavalue.getY()] = datavalue.getValue();
            pixelWriter.setColor(datavalue.getX(), datavalue.getY(), colorMap.get(datavalue.getValue()));
        }
    }

    private int getStopGrowingCondition() {
        int freeSpace = 0;
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (matrix[i][j] == 0) {
                    freeSpace++;
                }
            }
        }
        return freeSpace;
    }

    private int getNumberToSet(int indexElement, int x, int y) {
        List<Integer> mooreNeighbours = getNeighbors(indexElement, x, y);
        if (mooreNeighbours.isEmpty()) {
            return 0;
        } else {
            Map<Object, Long> groupedNeighbours = groupNeighbours(mooreNeighbours);
            return (int) getMaxNeighbour(groupedNeighbours);
        }
    }

    private int getShapeControlNumberToSet(int indexElement, int x, int y) {
        if (indexElement != 0) {
            return 0;
        }
        int first = checkFirstRule(indexElement, x, y);
        if (first != 0) {
            return first;
        }
        int second = checkSecondRule(x, y);
        if (second != 0) {
            return second;
        }
        int third = checkThirdRule(x, y);
        if (third != 0) {
            return third;
        }
        return checkForthRule(indexElement, x, y);
    }

    private int checkFirstRule(int indexElement, int x, int y) {
        List<Integer> neighbors = getNeighbors(indexElement, x, y);
        if (neighbors.isEmpty()) {
            return 0;
        } else {
            Map<Object, Long> groupedNeighbours = groupNeighbours(neighbors);
            group_info group_info = getMaxNeighbourInfo(groupedNeighbours);
            if (group_info.getSize() >= 5) {
                return group_info.getValue();
            }
            return 0;
        }
    }

    private List<Integer> getNeighbors(int indexElement, int x, int y) {
        List<Integer> neighbors = new ArrayList<>();
        switch (selection) {
            case NO:
                neighbors = getMooreInBorderNeighbors(indexElement, x, y);
                break;
            case YES:
                neighbors = getMooreOutBorderNeighbors(indexElement, x, y);
                break;
        }
        if (selectedGrainsForStructure != null && !selectedGrainsForStructure.isEmpty()) {
            neighbors = neighbors.stream().filter(num -> !selectedGrainsForStructure.contains(String.valueOf(num))).collect(Collectors.toList());
        }
        return neighbors;
    }

    private int checkForthRule(int indexElement, int x, int y) {
        Random random = new Random();
        if (random.nextInt(100) < probability) {
            return getNumberToSet(indexElement, x, y);
        }
        return 0;
    }

    private int checkThirdRule(int x, int y) {
        List<Integer> neighbors = getThirdRuleNeighbors(x, y);
        if (neighbors.isEmpty()) {
            return 0;
        } else {
            Map<Object, Long> groupedNeighbours = groupNeighbours(neighbors);
            group_info group_info = getMaxNeighbourInfo(groupedNeighbours);
            if (group_info.getSize() >= 3) {
                return group_info.getValue();
            }
            return 0;
        }
    }

    private List<Integer> getThirdRuleNeighbors(int x, int y) {
        switch (selection) {
            case NO:
                return getThirdRuleInBorderNeighbors(x, y);
            case YES:
                return getThirdRuleOutBorderNeighbors(x, y);
        }
        return new ArrayList<>();
    }

    private List<Integer> getThirdRuleInBorderNeighbors(int x, int y) {
        List<Integer> thirdRuleNeighbors = new ArrayList<>();
        if (y + 1 != ySize && x + 1 != xSize) {
            thirdRuleNeighbors.add(matrix[x + 1][y + 1]);
        }
        if (x - 1 != -1 && y + 1 != ySize) {
            thirdRuleNeighbors.add(matrix[x - 1][y + 1]);
        }
        if (x - 1 != -1 && y - 1 != -1) {
            thirdRuleNeighbors.add(matrix[x - 1][y - 1]);
        }
        if (x + 1 != xSize && y - 1 != -1) {
            thirdRuleNeighbors.add(matrix[x + 1][y - 1]);
        }
        return thirdRuleNeighbors.stream().filter(num -> num != 0 && num != -1).collect(Collectors.toList());
    }

    private List<Integer> getThirdRuleOutBorderNeighbors(int x, int y) {
        List<Integer> thirdRuleNeighbors = new ArrayList<>();
        if (y + 1 != ySize && x + 1 != xSize) {
            thirdRuleNeighbors.add(matrix[x + 1][y + 1]);
        } else {
            thirdRuleNeighbors.add(matrix[x + 1 != xSize ? x + 1 : 0][y + 1 != xSize ? y + 1 : 0]);
        }
        if (x - 1 != -1 && y + 1 != ySize) {
            thirdRuleNeighbors.add(matrix[x - 1][y + 1]);
        } else {
            thirdRuleNeighbors.add(matrix[x - 1 != -1 ? x - 1 : xSize - 1][y + 1 != ySize ? y + 1 : 0]);
        }
        if (x - 1 != -1 && y - 1 != -1) {
            thirdRuleNeighbors.add(matrix[x - 1][y - 1]);
        } else {
            thirdRuleNeighbors.add(matrix[x - 1 != -1 ? x - 1 : xSize - 1][y - 1 != -1 ? y - 1 : ySize - 1]);
        }
        if (x + 1 != xSize && y - 1 != -1) {
            thirdRuleNeighbors.add(matrix[x + 1][y - 1]);
        } else {
            thirdRuleNeighbors.add(matrix[x + 1 != xSize ? x + 1 : 0][y - 1 != -1 ? y - 1 : ySize - 1]);
        }
        return thirdRuleNeighbors.stream().filter(num -> num != 0 && num != -1).collect(Collectors.toList());
    }

    private int checkSecondRule(int x, int y) {
        List<Integer> neighbors = getSecondRuleNeighbors(x, y);
        if (neighbors.isEmpty()) {
            return 0;
        } else {
            Map<Object, Long> groupedNeighbours = groupNeighbours(neighbors);
            group_info group_info = getMaxNeighbourInfo(groupedNeighbours);
            if (group_info.getSize() >= 3) {
                return group_info.getValue();
            }
            return 0;
        }
    }

    private List<Integer> getSecondRuleNeighbors(int x, int y) {
        switch (selection) {
            case NO:
                return getSecondRuleInBorderNeighbors(x, y);
            case YES:
                return getSecondRuleOutBorderNeighbors(x, y);
        }
        return new ArrayList<>();
    }

    private List<Integer> getSecondRuleInBorderNeighbors(int x, int y) {
        List<Integer> secondRuleNeighbors = new ArrayList<>();
        if (y + 1 != ySize) {
            secondRuleNeighbors.add(matrix[x][y + 1]);
        }
        if (x + 1 != xSize) {
            secondRuleNeighbors.add(matrix[x + 1][y]);
        }
        if (x - 1 != -1) {
            secondRuleNeighbors.add(matrix[x - 1][y]);
        }
        if (y - 1 != -1) {
            secondRuleNeighbors.add(matrix[x][y - 1]);
        }
        return secondRuleNeighbors.stream().filter(num -> num != 0 && num != -1).collect(Collectors.toList());
    }

    private List<Integer> getSecondRuleOutBorderNeighbors(int x, int y) {
        List<Integer> secondRuleNeighbors = new ArrayList<>();
        if (y + 1 != ySize) {
            secondRuleNeighbors.add(matrix[x][y + 1]);
        } else {
            secondRuleNeighbors.add(matrix[x][0]);
        }
        if (x + 1 != xSize) {
            secondRuleNeighbors.add(matrix[x + 1][y]);
        } else {
            secondRuleNeighbors.add(matrix[0][y]);
        }
        if (x - 1 != -1) {
            secondRuleNeighbors.add(matrix[x - 1][y]);
        } else {
            secondRuleNeighbors.add(matrix[xSize - 1][y]);
        }
        if (y - 1 != -1) {
            secondRuleNeighbors.add(matrix[x][y - 1]);
        } else {
            secondRuleNeighbors.add(matrix[x][ySize - 1]);
        }
        return secondRuleNeighbors;
    }

    private List<Integer> getMooreInBorderNeighbors(int indexElement, int x, int y) {
        List<Integer> mooreNeighbours = new ArrayList<>();
        if (indexElement == 0) {
            if (y + 1 != ySize) {
                mooreNeighbours.add(matrix[x][y + 1]);
            }
            if (x + 1 != xSize) {
                mooreNeighbours.add(matrix[x + 1][y]);
            }
            if (y + 1 != ySize && x + 1 != xSize) {
                mooreNeighbours.add(matrix[x + 1][y + 1]);
            }
            if (x - 1 != -1) {
                mooreNeighbours.add(matrix[x - 1][y]);
            }
            if (x - 1 != -1 && y + 1 != ySize) {
                mooreNeighbours.add(matrix[x - 1][y + 1]);
            }
            if (x - 1 != -1 && y - 1 != -1) {
                mooreNeighbours.add(matrix[x - 1][y - 1]);
            }
            if (y - 1 != -1) {
                mooreNeighbours.add(matrix[x][y - 1]);
            }
            if (x + 1 != xSize && y - 1 != -1) {
                mooreNeighbours.add(matrix[x + 1][y - 1]);
            }
        }
        return mooreNeighbours.stream().filter(num -> num != 0 && num != -1).collect(Collectors.toList());
    }

    private List<Integer> getMooreOutBorderNeighbors(int indexElement, int x, int y) {
        List<Integer> mooreNeighbours = new ArrayList<>();
        if (indexElement == 0) {
            if (y + 1 != ySize) {
                mooreNeighbours.add(matrix[x][y + 1]);
            } else {
                mooreNeighbours.add(matrix[x][0]);
            }
            if (x + 1 != xSize) {
                mooreNeighbours.add(matrix[x + 1][y]);
            } else {
                mooreNeighbours.add(matrix[0][y]);
            }
            if (y + 1 != ySize && x + 1 != xSize) {
                mooreNeighbours.add(matrix[x + 1][y + 1]);
            } else {
                mooreNeighbours.add(matrix[x + 1 != xSize ? x + 1 : 0][y + 1 != xSize ? y + 1 : 0]);
            }
            if (x - 1 != -1) {
                mooreNeighbours.add(matrix[x - 1][y]);
            } else {
                mooreNeighbours.add(matrix[xSize - 1][y]);
            }
            if (x - 1 != -1 && y + 1 != ySize) {
                mooreNeighbours.add(matrix[x - 1][y + 1]);
            } else {
                mooreNeighbours.add(matrix[x - 1 != -1 ? x - 1 : xSize - 1][y + 1 != ySize ? y + 1 : 0]);
            }
            if (x - 1 != -1 && y - 1 != -1) {
                mooreNeighbours.add(matrix[x - 1][y - 1]);
            } else {
                mooreNeighbours.add(matrix[x - 1 != -1 ? x - 1 : xSize - 1][y - 1 != -1 ? y - 1 : ySize - 1]);
            }
            if (y - 1 != -1) {
                mooreNeighbours.add(matrix[x][y - 1]);
            } else {
                mooreNeighbours.add(matrix[x][ySize - 1]);
            }
            if (x + 1 != xSize && y - 1 != -1) {
                mooreNeighbours.add(matrix[x + 1][y - 1]);
            } else {
                mooreNeighbours.add(matrix[x + 1 != xSize ? x + 1 : 0][y - 1 != -1 ? y - 1 : ySize - 1]);
            }
        }
        return mooreNeighbours.stream().filter(num -> num != 0 && num != -1).collect(Collectors.toList());
    }

    private static Map<Object, Long> groupNeighbours(List<Integer> neighbours) {
        return neighbours.stream().collect(Collectors.groupingBy(num -> num, Collectors.counting()));
    }

    private static <K, V extends Comparable<V>> K getMaxNeighbour(Map<K, V> map) {
        return Collections.max(map.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private group_info getMaxNeighbourInfo(Map<Object, Long> map) {
        Map.Entry<Object, Long> entry = Collections.max(map.entrySet(), Map.Entry.comparingByValue());
        return new group_info(entry.getValue().intValue(), (Integer) entry.getKey());
    }
}

