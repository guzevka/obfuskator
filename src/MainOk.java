import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class MainOk {
    private static String transform = "";
    private static final Set<String> variables = new HashSet<>();
    private static final Set<String> functions = new HashSet<>();
    private static final Set<String> newNames = new HashSet<>();
    private static final Map<String, String> mapNameVariables = new HashMap<>();
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
                //Поиск имен функций:
                if (line.contains("function")) {
                    String functionName = getName("function", line);
                    //проверка, что функция не анонимная
                    if (!functionName.isEmpty() && !functionName.contains("(")) {
                        functions.add(functionName);
                    }
                }
                //Поиск имен переменных:
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

            // 3 Добавление пробелов
            // 4 Создание lines
            // 5 Записывание переменных в метод primarySearchVariables
            // 6 Замена имен переменных
            // 7 Добавление непрозрачных предикатов и недостижимого кода
            // 8 Удаление переноса строк
            // 9 Замена имен функций

            String[] lines = transform.split("\n");

            primarySearchVariables("let", lines);
            primarySearchVariables("var", lines);
            primarySearchVariables("const", lines);

            //Замена имен переменных при их использовании

            for (String variable : variables) {
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
                        int rndNumber = new Random().nextInt(predicatList.size());
                        String rndValue = predicatList.get(rndNumber);
                        lines[i] = lines[i].replace("if(", "if( " + rndValue + " && ");
                    }
                    //добавление недостижимого кода
                    if (lines[i].contains("return")) {
                        int rndIndex = new Random().nextInt(unachievableList.size());
                        lines[i + 1] = "\n; " + unachievableList.get(rndIndex) + lines[i + 1];
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
            for (String functionName : functions) {
                String newName = newNewNameGenerator();
                if (!newNames.contains(newName)) {
                    newNames.add(newName);
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
        for (String variables : variables) {
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