package is.fivefivefive.alloyasg.asg;
import com.abdulfatir.jcomplexnumber.ComplexNumber;

/*
 * A class representing the Magnetic Laplacian matrix.
 */
public class MagneticLaplacian {
    private ComplexNumber[][] matrix;
    private int size = 0;

    /*
     * Constructs an unnormalized Magnetic Laplacian matrix from the given ASGraph.
     * @param g The ASGraph to construct the Laplacian matrix from.
     * @param q The q parameter for the Laplacian matrix.
     */
    public MagneticLaplacian(ASGraph g, final double q) {
        this.size = g.getNumVertices();
        this.matrix = new ComplexNumber[size][size];
        for (int i = 0; i < size; ++i) {
            double diag = 0;
            for (int j = 0; j < size; ++j) {
                if (i == j) {
                    continue;
                }
                double as = (g.getEdgeWeight(i, j) + g.getEdgeWeight(j, i)) / 2.0;
                diag += as;
                double theta = 2 * Math.PI * q * (g.getEdgeWeight(i, j) - g.getEdgeWeight(j, i));
                ComplexNumber c = new ComplexNumber(as * Math.cos(theta), as * Math.sin(theta));
                matrix[i][j] = c;
            }
            matrix[i][i] = new ComplexNumber(diag, 0);
        }
    }
    /*
     * Output the Laplacian matrix to a CSV string so Python can read it.
     * @return A string representation of the Laplacian matrix.
     */
    public String toString() {
        String s = "";
        for (int i = 0; i < size; ++i) {
            // s += "[";
            for (int j = 0; j < size; ++j) {
                s += matrix[i][j].toString();
                if (j != size - 1) {
                    s += ", ";
                }
            }
            // s += "]";
            if (i != size - 1) {
                s += ",\n";
            }
        }
        return s;
    }
}
