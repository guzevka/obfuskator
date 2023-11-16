import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class MainOk {
    private static String transform = "";
    private static final Set<String> setVariables = new HashSet<>();
    private static final Set<String> setFunctions = new HashSet<>();
    private static final Set<String> setNewNames = new HashSet<>();
    private static final Map<String, String> mapNameVariables = new HashMap<>();
    //для добавления избыточности кода
    private static final List<String> redundantCodeList = Arrays.asList(
            "true", "!false", "5>0", "2**5 > 10**1", "true + 5",
            "5+4-15-518+92+8421*2/12*0 < 4892"
    );
    //для добавления избыточности кода
    private static final List<String> listUnreachableCode = Arrays.asList(
            "if(true>false){\n" +
                    "    while(false){\n" +
                    "        System.out.println(0);\n" +
                    "    }\n" +
                    "}",
            "() -> {\n" +
                    "    alert(eval(\"5+5\"));\n" +
                    "};",
            "(() -> {\n" +
                    "    alert('const ijkl = \"dspc,d\" + 5 - true;');\n" +
                    "})();",
            "if (false) {\n" +
                    "    \"IiIiiIII\".concat(\"iiIIii\");\n" +
                    "}",
            "if (5>0) {\n" +
                    "    new Date(\"December 25, 1995 23:15:00\").getDate();\n" +
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
                //Поиск имен функций:
                if (line.contains("function")) {
                    String functionName = getName("function", line);
                    //проверка, что функция не анонимная
                    if (!functionName.isEmpty() && !functionName.contains("(")) {
                        setFunctions.add(functionName);
                    }
                }
                //Поиск имен переменных:
                if (line.contains("let ")) {
                    setVariables.add(getName("let", line));
                }
                if (line.contains("var")) {
                    setVariables.add(getName("var", line));
                }
                if (line.contains("const")) {
                    setVariables.add(getName("const", line));
                }
            }


            transform = transform.replace(";", " ; ");
            transform = transform.replace("=", " = ");
            transform = transform.replace("(", " ( ");
            transform = transform.replace(")", " ) ");
            transform = transform.replace("{", " { ");
            transform = transform.replace("}", " } ");
            transform = transform.replace("[", " [ ");
            transform = transform.replace("]", " ] ");
            transform = transform.replace("+", " + ");
            transform = transform.replace("-", " - ");
            transform = transform.replace(".", " . ");
            transform = transform.replace(",", " , ");
            transform = transform.replace("`", " ` ");
            transform = transform.replace("\"", " \" ");
            String[] lines = transform.split("\n");


            primarySearchVariables("let", lines);
            primarySearchVariables("var", lines);
            primarySearchVariables("const", lines);

            //Замена имен переменных при их использовании

            for (String variable : setVariables) {
                for (int i = 0; i < lines.length; i++) {
                    line = lines[i];
                    int posVal = line.indexOf(variable);
                    if (posVal > 0 && !line.contains("\" " + variable + " \"")) {
                        lines[i] = line.replaceAll("(?<=\\W)" + variable + "(?=\\W)", mapNameVariables.get(variable));
                    } else if (line.chars().filter(ch -> ch == variable.charAt(0)).count() > 1 && line.contains("\" " + variable + " \"")) {
                        lines[i] = line.replaceFirst(Pattern.quote(variable), mapNameVariables.get(variable));
                    } else if (posVal == 0) {
                        lines[i] = line.replaceFirst(variable + "\\W", mapNameVariables.get(variable));
                    }
                }
            }



            for (int i = 0; i < lines.length; i++) {
                //Добавление избыточного кода:
                if (lines[i].contains("if(")) {
                    for(int j = 0; j < 5; j++) {
                        int rndNumber = new Random().nextInt(redundantCodeList.size());
                        String rndValue = redundantCodeList.get(rndNumber);
                        lines[i] = lines[i].replace("if(", "if( " + rndValue + " && ");
                    }
                    //добавление недостижимого кода
                    if (lines[i].contains("return")) {
                        int rndIndex = new Random().nextInt(listUnreachableCode.size());
                        lines[i + 1] = "\n; " + listUnreachableCode.get(rndIndex) + lines[i + 1];
                    }
                }
            }

            transform = String.join("\n", lines);


            //Удаление переносов строк:
            transform = transform.replaceAll("\\s(?!(\\n))", " ");
            //Удаление пробелов везде, кроме между слов:
            transform = transform.replaceAll("(?<=[A-z])\\s+(?=[A-z])", " ");
            //Удаление пробелов:
            transform = transform.replaceAll("\\B\\s+|\\s+\\B", "");
            //Документация и многострочный комментарий:
            transform = transform.replaceAll("/\\*[\\s\\S]*?\\*/", "");


            //Производим замену старых имен функций на новые
            for (String functionName : setFunctions) {
                String newName = newNewNameGenerator();
                if (!setNewNames.contains(newName)) {
                    setNewNames.add(newName);
                    transform = transform.replace(functionName + "(", newName + "(");
                }
            }

            writer.write(transform);
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getName(String keyword, String line) {
        String[] words = line.split(" |,|;|\\(|=");
        int ind = Arrays.asList(words).indexOf(keyword);
        if (ind != -1) {
            return words[ind + 1];
        }
        return "";
    }


    private static String newNewNameGenerator() {
        Random random = new Random();
        String name = "_x" + (random.nextInt(9000) + 1000) + (char) (random.nextInt(26) + 'a');
        return name;
    }




    private static void primarySearchVariables(String keyword, String[] lines) {
        //Проход по именам старых переменных
        for (String variables : setVariables) {
            String newName = newNewNameGenerator(); //берём результат функции выше newNewNameGenerator()
            //Проходим по строкам
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int indexLet = line.indexOf(keyword);
                //Если в строке есть ключевое слово, то ищем имя переменной
                if (indexLet != -1) {
                    int posVar = line.indexOf(variables);
                    if (posVar != -1 && line.charAt(posVar - 1) == ' ' && line.charAt(posVar + variables.length()) == ' ') {
                        mapNameVariables.put(variables, newName);
                        lines[i] = line.replace(variables, newName);
                    }
                }
            }
        }
    }


}