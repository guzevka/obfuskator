import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main1 {
    private static String transformedLine = "";
    private static final Set<String> setVariables = new HashSet<>();
    private static final Set<String> setFunctions = new HashSet<>();
    private static final Set<String> setNewNames = new HashSet<>();
    private static final Map<String, String> mapNameVariables = new HashMap<>();
    //для добавления избыточности кода
    private static final List<String> redundantCodeList = Arrays.asList(
            "true", "!false", "5>0", "2**5 > 10**1", "true + 5",
            "5+4-15-518+92+8421*2/12*0 < 4892"
    );

    public static void main(String[] args) {
        String inputFile = "C:\\Users\\walla\\Programms\\input.txt";
        String outputFile = "C:\\Users\\walla\\Programms\\output.txt";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                transformedLine += line + "\n";
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


//            transformedLine = transformedLine.replace(";", " ; ");
//            transformedLine = transformedLine.replace("=", " = ");
//            transformedLine = transformedLine.replace("(", " ( ");
//            transformedLine = transformedLine.replace(")", " ) ");
//            transformedLine = transformedLine.replace("{", " { ");
//            transformedLine = transformedLine.replace("}", " } ");
//            transformedLine = transformedLine.replace("[", " [ ");
//            transformedLine = transformedLine.replace("]", " ] ");
//            transformedLine = transformedLine.replace("+", " + ");
//            transformedLine = transformedLine.replace("-", " - ");
//            transformedLine = transformedLine.replace(".", " . ");
//            transformedLine = transformedLine.replace(",", " , ");
//            transformedLine = transformedLine.replace("`", " ` ");
//            transformedLine = transformedLine.replace("\"", " \" ");
            transformedLine = addSpace(transformedLine);
            String[] lines = transformedLine.split("\n");

            transformedLine = deleteTransfer(transformedLine);


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

            //Производим замену старых имен функций на новые
            for (String functionName : setFunctions) {
                String newName = newNewNameGenerator();
                if (!setNewNames.contains(newName)) {
                    setNewNames.add(newName);
                    transformedLine = transformedLine.replace(functionName + "(", newName + ")");
                }
            }


            for (int i = 0; i < lines.length; i++) {
                //Добавление избыточного кода:
                if (lines[i].contains("if(")) {
                    for (int j = 0; j < 5; j++) {
                        int rndNumber = new Random().nextInt(redundantCodeList.size());
                        String rndValue = redundantCodeList.get(rndNumber);
                        lines[i] = lines[i].replace("if(", "if( " + rndValue + " && ");
                    }
                }
            }

            transformedLine = String.join("\n", lines);


//            //Удаление переносов строк:
//            transformedLine = transformedLine.replaceAll("\\s(?!(\\n))", " ");
//            //Удаление пробелов везде, кроме между слов:
//            transformedLine = transformedLine.replaceAll("(?<=[A-z])\\s+(?=[A-z])", " ");
//            //Удаление пробелов:
//            transformedLine = transformedLine.replaceAll("\\B\\s+|\\s+\\B", "");



            //Производим замену старых имен функций на новые
            for (String functionName : setFunctions) {
                String newName = newNewNameGenerator();
                if (!setNewNames.contains(newName)) {
                    setNewNames.add(newName);
                    transformedLine = transformedLine.replace(functionName + "(", newName + "(");
                }
            }

            writer.write(transformedLine);
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

    private static String deleteTransfer(String transformedLine) {
        //Удаление переносов строк:
        transformedLine = transformedLine.replaceAll("\\s(?!(\\n))", " ");
        //Удаление пробелов везде, кроме между слов:
        transformedLine = transformedLine.replaceAll("(?<=[A-z])\\s+(?=[A-z])", " ");
        //Удаление пробелов:
        transformedLine = transformedLine.replaceAll("\\B\\s+|\\s+\\B", "");
        return transformedLine;
    }

    private static String addSpace(String transformedLine) {
        transformedLine = transformedLine.replace(";", " ; ");
        transformedLine = transformedLine.replace("=", " = ");
        transformedLine = transformedLine.replace("(", " ( ");
        transformedLine = transformedLine.replace(")", " ) ");
        transformedLine = transformedLine.replace("{", " { ");
        transformedLine = transformedLine.replace("}", " } ");
        transformedLine = transformedLine.replace("[", " [ ");
        transformedLine = transformedLine.replace("]", " ] ");
        transformedLine = transformedLine.replace("+", " + ");
        transformedLine = transformedLine.replace("-", " - ");
        transformedLine = transformedLine.replace(".", " . ");
        transformedLine = transformedLine.replace(",", " , ");
        transformedLine = transformedLine.replace("`", " ` ");
        transformedLine = transformedLine.replace("\"", " \" ");
        return transformedLine;
    }


}
