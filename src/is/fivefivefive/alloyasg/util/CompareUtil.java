package is.fivefivefive.alloyasg.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abdulfatir.jcomplexnumber.ComplexNumber;

import is.fivefivefive.alloyasg.etc.Triple;
import is.fivefivefive.alloyasg.vector.Vector2D;

public class CompareUtil {
    final static double DIFF = 1e-8;
    public static Triple<Vector2D, Vector2D, List<Double>> compareCSBASG(Vector2D asg1, Vector2D asg2, List<Double> signList1, List<Double> signList2) {
        Map<Double, Integer> signIds = new HashMap<>();
        List<Double> signList = new ArrayList<>();
        signList.add(0.0);
        signList.addAll(signList1);
        for (int i = 1; i <= signList1.size(); ++i) {
            signIds.put(signList1.get(i - 1), i);
        }
        for (int i = 0; i < signList2.size(); ++i) {
            if (!signIds.containsKey(signList2.get(i))) {
                signList.add(signList2.get(i));
                signIds.put(signList2.get(i), signIds.size() + 1);
            }
        }
        int totalSize = signIds.size();
        Vector2D asg1C = new Vector2D(totalSize + 1);
        Vector2D asg2C = new Vector2D(totalSize + 1);
        double root2 = signList2.get(0);
        int root2Id = signIds.get(root2);
        asg1C.set(0, 1, new ComplexNumber(1));
        asg2C.set(0, root2Id, new ComplexNumber(1));
        for (int i = 0; i < asg1.size(); ++i) {
            for (int j = 0; j < asg1.size(); ++j) {
                if (i == 0 && j == 0) {
                    continue;
                }
                asg1C.set(i + 1, j + 1, asg1.get(i, j));
            }
        }
        for (int i = 0; i < asg2.size(); ++i) {
            for (int j = 0; j < asg2.size(); ++j) {
                if (i == 0 && j == 0) {
                    continue;
                }
                double sign1 = signList2.get(i);
                double sign2 = signList2.get(j);
                int sign1Id = signIds.get(sign1);
                int sign2Id = signIds.get(sign2);
                asg2C.set(sign1Id, sign2Id, asg2.get(i, j));
            }
        }
        return new Triple<Vector2D, Vector2D, List<Double>>(asg1C, asg2C, signList);
    }
}
