package is.fivefivefive.alloyasg.vector;
import com.abdulfatir.jcomplexnumber.ComplexNumber;
import java.util.List;
import java.util.ArrayList;

public class Vector1D {
    private List<ComplexNumber> vector;
    private int size;

    public Vector1D(int size) {
        this.size = size;
        vector = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            vector.add(new ComplexNumber(0));
        }
    }

    public Vector1D(ComplexNumber[] vector) {
        this.size = vector.length;
        this.vector = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            this.vector.add(vector[i]);
        }
    }

    public Vector1D(List<ComplexNumber> vector) {
        this.vector = vector;
        this.size = vector.size();
    }

    public int size() {
        return size;
    }

    public static Vector1D fromReal(List<Double> relVector) {
        int size = relVector.size();
        List<ComplexNumber> colVector = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            colVector.add(new ComplexNumber(relVector.get(i)));
        }
        return new Vector1D(colVector);
    }

    public static Vector1D fromReal(double[] relVector) {
        int size = relVector.length;
        List<ComplexNumber> colVector = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            colVector.add(new ComplexNumber(relVector[i]));
        }
        return new Vector1D(colVector);
    }
    
    public ComplexNumber get(int i) {
        return vector.get(i);
    }

    public ComplexNumber innerProduct(Vector1D other) {
        ComplexNumber sum = new ComplexNumber(0);
        for (int i = 0; i < size; ++i) {
            ComplexNumber.multiply(this.get(i), other.get(i));
            sum.add(vector.get(i));
        }
        return sum;
    }

    public Vector1D elemProduct(Vector1D other) {
        List<ComplexNumber> prod = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            prod.add(ComplexNumber.multiply(this.get(i), other.get(i)));
        }
        return new Vector1D(prod);
    }

    public Vector1D add(Vector1D other) {
        List<ComplexNumber> sum = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            sum.add(ComplexNumber.add(this.get(i), other.get(i)));
        }
        return new Vector1D(sum);
    }

    public Vector1D subtract(Vector1D other) {
        List<ComplexNumber> diff = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            diff.add(ComplexNumber.subtract(this.get(i), other.get(i)));
        }
        return new Vector1D(diff);
    }

    public Vector1D multiply(ComplexNumber scalar) {
        List<ComplexNumber> prod = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            prod.add(ComplexNumber.multiply(this.get(i), scalar));
        }
        return new Vector1D(prod);
    }

    public Vector1D divide(ComplexNumber scalar) {
        List<ComplexNumber> quot = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            quot.add(ComplexNumber.divide(this.get(i), scalar));
        }
        return new Vector1D(quot);
    }

    public Vector1D conjugate() {
        List<ComplexNumber> conj = new ArrayList<ComplexNumber>(size);
        for (int i = 0; i < size; ++i) {
            conj.add(this.get(i).conjugate());
        }
        return new Vector1D(conj);
    }

    public ComplexNumber[] toArray() {
        return vector.toArray(new ComplexNumber[size]);
    }



    public String toString() {
        String rep = "";
        for (int i = 0; i < size; ++i) {
            rep += vector.get(i).toString() + ", ";
        }
        rep += "\n";
        return rep;
    }


}
