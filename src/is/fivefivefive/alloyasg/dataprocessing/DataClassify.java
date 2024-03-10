package is.fivefivefive.alloyasg.dataprocessing;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.parser.CompModule;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;

public class DataClassify {
    public static void main(String[] args) throws IOException {
        makeClassifier("AlloyParser/data", "AlloyParser/results/classified-data");
    }

    private static void makeClassifier(String dataDir, String resultDir) throws IOException {
        File dataFolder = new File(dataDir);
        File[] fList = dataFolder.listFiles();
        for (File f: fList) {
            if (f.isFile()) {
                String cat = "DEFAULT";
                try {
                    cat = classify(f.getAbsolutePath());
                }
                catch (Exception e) {
                    System.out.println("Runtime error in model " + f.getAbsolutePath());
                    continue;
                }
                File resultFolder = new File(resultDir + "/" + cat);
                if (!resultFolder.exists()) {
                    Files.createDirectories(Paths.get(resultDir + "/" + cat));
                }
                Files.copy(f.toPath(), Paths.get(resultFolder.getAbsolutePath() + "/" + f.getName()), StandardCopyOption.REPLACE_EXISTING);
            } else {
                makeClassifier(f.getAbsolutePath(), resultDir + "/" + f.getName());
            }
        }
    }

    // output: the class of correctness
    private static String classify(String modelFile) {
        A4Reporter rep = new A4Reporter() {
            @Override
            public void warning(ErrorWarning msg) {
                System.out.println(msg.toString().trim());
                System.out.flush();
            }
	    };
        CompModule world = CompUtil.parseEverything_fromFile(rep, null, modelFile);
        Command command0 = world.getAllCommands().get(0);
        Command command1 = world.getAllCommands().get(1);
        A4Options options = new A4Options();
        options.solver = A4Options.SatSolver.SAT4J;
        A4Solution instance0 = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command0, options);
        A4Solution instance1 = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command1, options);
        if (instance0.satisfiable()) {
            if (instance1.satisfiable()) {
                return "BOTH";
            } else {
                return "OVERCONSTRAINED";
            }
        } else if (instance1.satisfiable()) {
            return "UNDERCONSTRAINED";
        } else {
            return "CORRECT";
        }
    }
}
