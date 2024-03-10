package is.fivefivefive.alloyasg.dataprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import edu.mit.csail.sdg.parser.CompModule;
import parser.ast.nodes.ModelUnit;
import parser.ast.visitor.PrettyStringVisitor;
import parser.util.AlloyUtil;
import parser.util.FileUtil;

public class SyntacticFilter {
  private static void parse(File file, String folder) throws IOException {
    //Read in Alloy file
	  CompModule module = AlloyUtil.compileAlloyModule(file.getAbsolutePath());
      ModelUnit mu = new ModelUnit(null, module);
    
      //This is one visitor pattern over the model, it visits node in the AST and prints it
      //Producing a replica of the Alloy model
      PrettyStringVisitor psv = new PrettyStringVisitor();
      //Writing it to a file here
      File directory = new File("./results/data/" + folder);
      if (!directory.exists()) {
        Files.createDirectories(Paths.get("./results/data/" + folder));
      }
      FileUtil.writeText(mu.accept(psv,null),"./results/data/" + folder + "/parsed-" + file.getName(),true);
  }

  private static void listParseFiles(String dire) throws IOException {
	  File directory = new File(dire); // /models
    File[] fList = directory.listFiles();
    for (File file : fList) {
      if (file.isFile()) {
        String folder = file.getParentFile().getName();
        try {
          parse(file, folder);
        } catch (Exception e) {
          System.out.println("Syntactic Error in " + file.getAbsolutePath());
        }
      } else if (file.isDirectory()) {
        listParseFiles(file.getAbsolutePath());
      }
    }
  }

  public static void main(String[] args) throws IOException {
    listParseFiles("models/");
  }
}