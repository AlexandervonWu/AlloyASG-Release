// TODO
package is.fivefivefive.alloyasg.vector;

import com.abdulfatir.jcomplexnumber.ComplexNumber;

import is.fivefivefive.alloyasg.representations.NodeRepresentation;
import is.fivefivefive.alloyasg.util.ODUtil;

import java.util.List;

public class Vector2D {
    private ComplexNumber[][] matrix;
    private int size;

    public Vector2D(int size) {
        this.size = size;
        matrix = new ComplexNumber[size][size];
        for (int i = 0; i < size; ++i) {
            matrix[i] = new ComplexNumber[size];
            for (int j = 0; j < size; ++j) {
                matrix[i][j] = new ComplexNumber(0);
            }
        }
    }
    
    public Vector2D(List<Vector1D> matrix, int mode) {
        if (mode == 1) {
            // row vectors
            this.size = matrix.size();
            this.matrix = new ComplexNumber[size][size];
            for (int i = 0; i < size; ++i) {
                this.matrix[i] = matrix.get(i).toArray();
            }
        } else {
            // column vectors
            this.size = matrix.get(0).size();
            this.matrix = new ComplexNumber[size][size];
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < matrix.size(); ++j) {
                    this.matrix[i][j] = matrix.get(j).get(i);
                }
            }
        }
    }

    public ComplexNumber[][] getMatrix() {
        return matrix;
    }

    public int size() {
        return size;
    }

    public ComplexNumber get(int i, int j) {
        return matrix[i][j];
    }

    public void set(int i, int j, ComplexNumber value) {
        matrix[i][j] = value;
    }

    public Vector2D perEntryProduct(Vector2D other) {
        Vector2D prod = new Vector2D(size);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < other.size; ++j) {
                prod.matrix[i][j] = ComplexNumber.multiply(this.get(i, j), other.get(i, j));
            }
        }
        return prod;
    }

    public Vector2D add(Vector2D other) {
        Vector2D sum = new Vector2D(size);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < other.size; ++j) {
                sum.matrix[i][j] = ComplexNumber.add(this.get(i, j), other.get(i, j));
            }
        }
        return sum;
    }

    public Vector2D subtract(Vector2D other) {
        Vector2D diff = new Vector2D(size);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < other.size; ++j) {
                diff.matrix[i][j] = ComplexNumber.subtract(this.get(i, j), other.get(i, j));
            }
        }
        return diff;
    }

    public Vector2D multiply(ComplexNumber scalar) {
        Vector2D prod = new Vector2D(size);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j){
                prod.matrix[i][j] = ComplexNumber.multiply(this.get(i, j), scalar);
            }
        }
        return prod;
    }

    public Vector2D multiply(Vector2D other) {
        Vector2D prod = new Vector2D(size);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < other.size; ++j) {
                for (int k = 0; k < size; ++k) {
                    prod.matrix[i][j] = ComplexNumber.add(prod.get(i, j), ComplexNumber.multiply(this.get(i, k), other.get(k, j)));
                }
            }
        }
        return prod;
    }

    public String toString() {
        String str = "";
        for (int i = 0; i < size; ++i) {
            //str += "[";
            for (int j = 0; j < size; ++j) {
                str += matrix[i][j].toString("rcispi", 2);
                if (j < size - 1) {
                    str += ", ";
                }
            }
            str += "\n";
        }
        return str;
    }

    public Vector2D clone() {
        Vector2D clone = new Vector2D(size);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                clone.matrix[i][j] = this.get(i, j);
            }
        }
        return clone;
    }

    public int numberNonZeroEntries() {
        int count = 0;
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                if (matrix[i][j].mod() > 0) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public static String toRealAdjacencyTable(Vector2D ve, List<Double> signs, int mode) {
        int size = ve.size();
        ComplexNumber[][] matrix = ve.getMatrix();
        String str = mode == 0 ? "[" : "https://chart.googleapis.com/chart?cht=gv&chl=digraph{";
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                if (matrix[i][j].mod() > 0) {
                    int value = (int)Math.round(matrix[i][j].mod());
                    double sign = signs.get(i);
                    int syntacticCategory = (int)Math.round(sign / NodeRepresentation.UNIT);
                    int maxChildren = ODUtil.getMaxChildrenBySyntax(syntacticCategory);
                    int k = 0;
                    while (value > 0) {
                        int positionBool = value % 2;
                        if (positionBool == 1) {
                            int times = k / maxChildren + 1;
                            int omega = -114514;
                            try {
                                omega = k % maxChildren + 1;
                            } catch (Exception e) {
                                System.out.println(syntacticCategory);
                                e.printStackTrace();
                            }
                            if (mode == 0) { // Python 
                                str += "(" + i + ", " + j + ", (" + times + "," + omega + ")) \n";
                            } else if (mode == 1) { // Google API, red graph
                                str += i + "->" + j + "[label=\"(" + times + "," + omega + ")\",color=\"red\"];";
                            } else if (mode == 2) { // Google API, blue graph
                                str += i + "->" + j + "[label=\"(" + times + "," + omega + ")\",color=\"blue\"];";
                            }
                            
                        }
                        value /= 2;
                        k++;
                    }
                    
                }
            }
        }
        str += mode == 0 ? "]" : "}";
        return str;
    }
}
