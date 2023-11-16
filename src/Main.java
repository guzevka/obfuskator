import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    private static final List<String> unvisiblePredicatList = Arrays.asList(
            "true", "!false", "5>0", "2**5 > 10**1"
    );

    private static final Set<String> setVariables = new HashSet<>();
    private static final Map<String, String> mapNameVariables = new HashMap<>();

    private static final List<String> unachievableList = Arrays.asList(
            "if(true>false){\n" +
                    "    while(false){\n" +
                    "        System.out.println(0);\n" +
                    "    }\n" +
                    "}",
            "if (5+4-15-518+92+8421*2/12*0 < 4892) {\n" +
                    "    Math.sin(Math.PI/2) + 1 - 20;\n" +
                    "}"
    );

    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\walla\\Programms\\input.txt";
        String outputFilePath = "C:\\Users\\walla\\Programms\\output.txt";

        try {
            File inputFile = new File(inputFilePath);
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            File outputFile = new File(outputFilePath);
            FileWriter fileWriter = new FileWriter(outputFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            String line = "";
            //String[] lines = line.split("\n");
            while ((line = bufferedReader.readLine()) != null) {

                //String transformedLine = line + "\n";
                 String transformedLine = deleteTransfer(line);
                //transformedLine = deleteTransfer(transformedLine);
                transformedLine = unvisiblePredicat(transformedLine);
                transformedLine = setNewName(transformedLine);
                //transformedLine = setNewName(transformedLine);
                //transformedLine = unachievable(transformedLine);

                bufferedWriter.write(transformedLine);
                bufferedWriter.newLine();

            }

            bufferedReader.close();
            bufferedWriter.close();

            System.out.println("Данные успешно прочитаны, преобразованы и записаны в другой файл.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод обфускации, удаляющий пробелы, перенос каретки и комментарии
    public static String deleteTransfer(String line) {
        line = line.replaceAll("\\s(?!(\\n))", " ");
        line = line.replaceAll("(?<=[A-z])\\s+(?=[A-z])", " ");
        line = line.replaceAll("\\B\\s+|\\s+\\B", "");
        line = line.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        return line;
    }

    // Метод обфускации с непрозрачными предикатами
    public static String unvisiblePredicat(String line) {
        String[] lines = line.split("\n");
        for(int i=0; i<lines.length; i++) {
            if(lines[i].contains("if(")) {
                for(int j=0; j<4; j++) {
                    int random = new Random().nextInt(unvisiblePredicatList.size());
                    String value = unvisiblePredicatList.get(random);
                    lines[i] = lines[i].replace("if(", "if(" + value + "&&");
                }

            }
        }
        line = String.join("\n", lines);
        return line;
    }

    // Метод обфускации, меняющий название переменной
    public static String setNewName(String line) {

        String[] lines = line.split("\\s+");

        if (line.contains("let ")) {
            setVariables.add(getName("let", line));
        }
        if (line.contains("var")) {
            setVariables.add(getName("var", line));
        }
        if (line.contains("const")) {
            setVariables.add(getName("const", line));
        }

        primarySearchVariables("let", lines);
        primarySearchVariables("var", lines);
        primarySearchVariables("const", lines);

//        for (String variable : setVariables) {
//
//            for (int i = 0; i < lines.length; i++) {
//                line = lines[i];
//                int posVal = line.indexOf(variable);
//                if (posVal > 0 && !line.contains("\" " + variable + " \"")) {
//                    lines[i] = line.replaceAll("(?<=\\W)" + variable + "(?=\\W)", mapNameVariables.get(variable));
//                } else if (line.chars().filter(ch -> ch == variable.charAt(0)).count() > 1 && line.contains("\" " + variable + " \"")) {
//                    lines[i] = line.replaceFirst(Pattern.quote(variable), mapNameVariables.get(variable));
//                } else if (posVal == 0) {
//                    lines[i] = line.replaceFirst("(?<!\\w)" + Pattern.quote(variable) + "(?!\\w)", mapNameVariables.get(variable));
//                }
//            }
//        }

        /////
//        String[] words = line.split("\\s+"); // Split the line into words
//
//        for (int i = 0; i < words.length; i++) {
//            String word = words[i];
//            if (word.matches("[a-zA-Z_$][a-zA-Z_$0-9]*")) { // Check if the word is a valid variable name
//                String newName = nameGenerator(); // Generate a new variable name
//                words[i] = word.replaceFirst("\\b" + word + "\\b", newName); // Replace the old variable name with the new one
//            }
//        }

        return String.join(" ", lines); // Join the modified words back into a line
    }

    private static void primarySearchVariables(String keyword, String[] lines) {
        //Проход по именам старых переменных
        for (String variables : setVariables) {
            String newName = nameGenerator(); //берём результат функции выше newNewNameGenerator()
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
    public static String nameGenerator() {
        Random random = new Random();
        String name = "_x" + (random.nextInt(9000) + 1000) + (char) (random.nextInt(26) + 'a');
        return name;
    }

    private static String getName(String keyword, String line) {
        String[] words = line.split(" |,|;|\\(|=");
        int ind = Arrays.asList(words).indexOf(keyword);
        if (ind != -1) {
            return words[ind + 1];
        }
        return "";
    }

    // Метод обфускации, добавляющий недостижимый код
//    public static String unachievable(String line) {
//        String[] lines = line.split("\n");
//        for (int i = 0; i < lines.length; i++) {
//            if (lines[i].contains("return")) {
//                for (int j = 0; j < lines.length - i - 1; j++) {
//                    int random = new Random().nextInt(unachievableList.size());
//                    if (i + 1 < lines.length) {
//                        lines[i + 1] = unachievableList.get(random) + lines[i + 1];
//                    }
//                }
//            }
//        }
//        line = String.join("\n", lines);
//        return line;
//    }

    // Мертвый код
    public static String deadCode(String line, boolean isDeadCodeAdded) {
        if (isDeadCodeAdded) {
            return line;
        } else {
            System.out.println("Добавление мертвого кода");
            line += "\nlet c = 5; \nlet d = 10; \nif(c>d) {\n console.log('Этот код не выполнится');\n}else{\nconsole.log('Этот код выполнится');\n}";
        }
         return line;
    }

    public static String noOtstup(String line) {
        line = line.replaceAll("\n", "");
        line = line.trim().replaceAll(" +", " ");
        return line;
    }

    // Непрозрачные предикаты (ПО-ДРУГОМУ)
    private static String predicat(String line) {
        String obfuscatedCode = "";
        for (char c : line.toCharArray()) {
            if (c == '>') {
                obfuscatedCode += "isGreaterThan";
            } else if (c == '<') {
                obfuscatedCode += "isLessThan";
            } else {
                obfuscatedCode += c;
            }
        }
        return obfuscatedCode;
    }

    // Методы обфускации
    private static String obfuskate(String line) {
        // Добавление лишних комментариев
        //line = "// This is a random comment\n" + line;
        // Добавление лишних пробелов
        line = line.replaceAll("\\s+", " ");
        // Избыточный код
        line = line.replace("e.printStackTrace();", " ");
        // Удаление отступов
        line = line.trim();
        return line;
    }

    public static String renameVariables(String line) {
        // Пример простого переименования переменных
        line = line.replace("inputFilePath", "randomVariable1");
        line = line.replace("outputFilePath", "randomVariable2");

        return line;
    }

    public static String addUnreachableCode(String line) {
        // Пример внесения недостижимого кода
        line += "\nSystem.exit(0);";

        return line;
    }

    public static String addRedundantCode(String line) {
        // Пример внесения избыточного кода
        line += "\nint x = 5 + 10;";

        return line;
    }


    private static String transformData(String data) {
        // Выполнение преобразований над данными
        // Например, можно заменить определенные символы или выполнить другие манипуляции с текстом
        return data.toUpperCase();
    }
}