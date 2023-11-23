import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    private static String transform = "";
    private static final Set<String> variables = new HashSet<>(); // старые названия переменных
    private static final Set<String> functions = new HashSet<>(); // старые названия функций
    private static final Set<String> newFunctions = new HashSet<>(); // новые названия функций
    private static final Map<String, String> newVariables = new HashMap<>(); // новые названия переменных
    private static final List<String> predicatList = Arrays.asList(
            "true", "!false", "5>0", "2**5 > 10**1"
    );
    private static final List<String> unachievableList = Arrays.asList(
            "if (5>0) {\n" +
                    "    new Date(\"October 17, 2003 23:15:00\").getDate();\n" +
                    "}",
            "if (5+4-15-518+92+8421*2/12*0 < 4892) {\n" +
                    "    Math.sin(Math.PI/2) + 1 - 20;\n" +
                    "}"
    );

    public static void main(String[] args) {
        String inputFile = "C:\\Users\\walla\\Programms\\Java\\obfuskator\\src\\input.js";
        String outputFile = "C:\\Users\\walla\\Programms\\Java\\obfuskator\\src\\output.js";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                transform += line + "\n";

                if (line.contains("function")) {
                    String functionName = getName("function", line);

                    if (!functionName.isEmpty() && !functionName.contains("(")) {
                        functions.add(functionName);
                    }
                }

                if (line.contains("let")) {
                    variables.add(getName("let", line));
                }
                if (line.contains("var")) {
                    variables.add(getName("var", line));
                }
                if (line.contains("const")) {
                    variables.add(getName("const", line));
                }
            }

            String[] lines = transform.split("\n");

            primarySearchVariables("let", lines);
            primarySearchVariables("var", lines);
            primarySearchVariables("const", lines);

            transform = replaceNameVariable(lines); // меняю имена переменных
            transform = addPredicat(lines,2); // добавляю предикаты
            transform = addUnavailable(lines); // добавляю недостижимый код
            transform = String.join("\n", lines);
            transform = deleteTransform(transform); // удаляю пробелы и переносы
            transform = replaceNameFunction(transform); // меняю имена функций

            writer.write(transform);
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String addUnavailable(String[] lines) {
        String line = "";
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("return")) {
                int rndIndex = new Random().nextInt(unachievableList.size());
                lines[i + 1] = "\n; " + unachievableList.get(rndIndex) + lines[i + 1];
            }
        }
        line = String.join("\n", lines);
        return line;
    }

    private static String deleteTransform(String line) {
        // переносы строк
        line = line.replaceAll("\\s(?!(\\n))", " ");
        // пробелы (кроме промежутков между слов)
        line = line.replaceAll("(?<=[A-z])\\s+(?=[A-z])", " ");
        return line;
    }

    private static String replaceNameFunction(String line) {
        for (String functionName : functions) {
            String newName = nameGenerator();
            if (!newFunctions.contains(newName)) {
                newFunctions.add(newName);
                line = line.replace(functionName + "(", newName + "(");
            }
        }
        return line;
    }

    public static String replaceNameVariable(String[] lines) {
        String line = "";
        for (String variable : variables) {
            for (int i = 0; i < lines.length; i++) {
                line = lines[i];
                int posVal = line.indexOf(variable);
                if (posVal > 0 && !line.contains("\" " + variable + " \"")) {
                    lines[i] = line.replaceAll("(?<=\\W)" + variable + "(?=\\W)", newVariables.get(variable));
                } else if (line.chars().filter(ch -> ch == variable.charAt(0)).count() > 1 && line.contains("\" " + variable + " \"")) {
                    lines[i] = line.replaceFirst(Pattern.quote(variable), newVariables.get(variable));
                } else if (posVal == 0) {
                    lines[i] = line.replaceFirst(variable + "\\W", newVariables.get(variable));
                }
            }
        }
        line = String.join("\n", lines);
        return line;
    }

    public static String addPredicat(String[] lines, int count) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("if(")) {
                for(int j = 0; j < count; j++) {
                    int rndNumber = new Random().nextInt(predicatList.size());
                    String rndValue = predicatList.get(rndNumber);
                    lines[i] = lines[i].replace("if(", "if( " + rndValue + " && ");
                }
            }
        }
        String line = String.join("\n", lines);
        return line;
    }

    private static String getName(String keyword, String line) {
        String[] words = line.split(" |,|;|\\(|=");
        int ind = Arrays.asList(words).indexOf(keyword);
        if (ind != -1) {
            return words[ind + 1];
        }
        return "";
    }


    private static String nameGenerator() {
        Random random = new Random();
        return "_x" + (random.nextInt(9000) + 1000) + (char) (random.nextInt(26) + 'a');
    }

    private static void primarySearchVariables(String keyword, String[] lines) {

        for (String variables : variables) {
            String newName = nameGenerator();
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int indexLet = line.indexOf(keyword);
                if (indexLet != -1) {
                    int posVar = line.indexOf(variables);
                    if (posVar != -1 && line.charAt(posVar - 1) == ' ' && line.charAt(posVar + variables.length()) == ' ') {
                        newVariables.put(variables, newName);
                        lines[i] = line.replace(variables, newName);
                    }
                }
            }
        }
    }


}