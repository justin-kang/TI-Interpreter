import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;

public class Interpreter {

    private HashMap<String, Double> variables;
    private HashMap<String, Double> constants;
    private boolean degrees;
    private boolean bool;
    private boolean malformed;
    private boolean print;
    private double value;

    private boolean isVar(String s) { return variables.containsKey(s); }

    private boolean isVal(String s) {
        try { Float.parseFloat(s); }
        catch (NumberFormatException e) { return false; }
        return true;
    }

    private boolean isBool(String s) {
        if (s.contains("<") || s.contains(">") || s.contains("!=") ||
                s.contains("==")) {
            bool = true;
            if (s.startsWith(">") || s.endsWith(">") || s.startsWith("<") ||
                    s.endsWith("<") || s.startsWith("=") || s.endsWith("=") ||
                    s.startsWith("!") || s.endsWith("!")) {
                malformed = true;
                return true;
            }
        }
        else
            bool = false;
        return bool;
    }

    private void store(String s) {
        if (malformed)
            return;
        if (s.indexOf("->") == 0 || s.indexOf("->") == s.length()-1) {
            malformed = true;
            return;
        }
        String var = s.substring(0, s.indexOf("->"));
        double val = sort(s.substring(s.indexOf("->")+1));
        variables.put(var, val);
    }

    private double bool(String s) {
        if (malformed)
            return 0;
        double left;
        double right;
        if (s.contains(">=")) {
            left = sort(s.substring(0, s.indexOf(">=")));
            right = sort(s.substring(s.indexOf(">=")+2));
            if (left >= right)
                return 1;
        }
        else if (s.contains("<=")) {
            left = sort(s.substring(0, s.indexOf("<=")));
            right = sort(s.substring(s.indexOf("<=")+2));
            if (left <= right)
                return 1;
        }
        else if (s.contains(">")) {
            left = sort(s.substring(0, s.indexOf(">")));
            right = sort(s.substring(s.indexOf(">")+1));
            if (left > right)
                return 1;
        }
        else if (s.contains("<")) {
            left = sort(s.substring(0, s.indexOf("<")));
            right = sort(s.substring(s.indexOf("<")+1));
            if (left < right)
                return 1;
        }
        else if (s.contains("==")) {
            left = sort(s.substring(0, s.indexOf("==")));
            right = sort(s.substring(s.indexOf("==")+2));
            if (left == right)
                return 1;
        }
        else if (s.contains("!=")) {
            left = sort(s.substring(0, s.indexOf("!=")));
            right = sort(s.substring(s.indexOf("!=")+2));
            if (left != right)
                return 1;
        }
        return 0;
    }

    private String subParentheses(String s) {
        if (malformed)
            return "";
        int count = 0;
        int index = 0;
        boolean set = false;
        for (char c : s.toCharArray()) {
            if (c == '(') {
                count++;
                set = true;
            }
            else if (c == ')')
                count--;
            index++;
            if (count == 0 && set)
                break;
        }
        if (count != 0) {
            malformed = true;
            return "";
        }
        if (index == s.length())
            return s;
        return s.substring(0, index);
    }

    private double log(String s) {
        if (malformed)
            return 0;
        double arg;
        if (s.contains(",")) {
            arg = sort(s.substring(s.indexOf("(")+1, s.lastIndexOf(",")));
            double base = sort(s.substring(s.lastIndexOf(",")+1, 
                                            s.lastIndexOf(")")));
            if (arg == 0) {
                malformed = true;
                return 0;
            }
            arg = Math.log(arg)/Math.log(base);
        }
        else {
            arg = sort(s.substring(s.indexOf("(")+1, s.lastIndexOf(")")));
            if (arg == 0) {
                malformed = true;
                return 0;
            }
            arg = Math.log10(arg);
        }
        if (Double.isNaN(arg)) {
            malformed = true;
            return 0;
        }
        return arg;
    }

    private double ln(String s) {
        if (malformed)
            return 0;
        double arg = sort(s.substring(s.indexOf("(")+1, s.lastIndexOf(")")));
        if (arg == 0) {
            malformed = true;
            return 0;
        }
        return Math.log(arg);
    }

    private double sum(String s, int op) {
        if (malformed) {
            return 0;
        }
        if (s.length() - s.replaceAll(",", "").length() < 3) {
            malformed = true;
            return 0;
        }
        double val = 0;
        double v = 0;
        boolean stored = false;
        int u = (int)sort(s.substring(s.lastIndexOf(",")+1, 
                                        s.lastIndexOf(")")));
        s = s.substring(0, s.lastIndexOf(","));
        int l = (int)sort(s.substring(s.lastIndexOf(",")+1));
        s = s.substring(0, s.lastIndexOf(","));
        String var = s.substring(s.lastIndexOf(",")+1);
        if (variables.containsKey(var)) {
            stored = true;
            v = variables.get(var);
        }
        String arg = s.substring(s.indexOf("sum(")+4, s.lastIndexOf(","));
        if (l > u) {
            if (op == 0)
                return 0;
            else if (op == 1)
                return 1;
            else {
                malformed = true;
                return 0;
            }
        }
        for (int i = l; i <= u; i++) {
            variables.put(var, (double)i);
            if (op == 0)
                val += sort(arg);
            else if (op == 1) {
                if (i == l)
                    val = sort(arg);
                else
                    val *= sort(arg);
            }
            else {
                malformed = true;
                return 0;
            }
        }
        if (stored)
            variables.put(var, v);
        else
            variables.remove(var);
        return val;
    }

    private double mod(String s) {
        if (malformed)
            return 0;
        if (!s.contains(",")) {
            malformed = true;
            return 0;
        }
        double dividend = sort(s.substring(s.indexOf("mod(")+4, 
                                            s.lastIndexOf(",")));
        double divisor = sort(s.substring(s.lastIndexOf(",")+1, 
                                           s.lastIndexOf(")")));
        return dividend % divisor;
    }

    private double round(String s) {
        if (malformed)
            return 0;
        if (!s.contains(",")) {
            malformed = true;
            return 0;
        }
        double arg = sort(s.substring(s.indexOf("round(")+6, 
                                       s.lastIndexOf(",")));
        double round = sort(s.substring(s.lastIndexOf(",")+1, 
                                         s.lastIndexOf(")")));
        round = Math.pow(10.0, round);
        int cast = (int)Math.round(arg * round);
        return cast / round;
    }

    private double integrate(String s) {
        if (malformed)
            return 0;
        if (s.length() - s.replaceAll(",", "").length() < 3) {
            malformed = true;
            return 0;
        }
        double val = 0;
        double v = 0;
        boolean stored = false;
        double u = sort(s.substring(s.lastIndexOf(",")+1, s.lastIndexOf(")")));
        s = s.substring(0, s.lastIndexOf(","));
        double l = sort(s.substring(s.lastIndexOf(",")+1));
        s = s.substring(0, s.lastIndexOf(","));
        String var = s.substring(s.lastIndexOf(",")+1);
        if (variables.containsKey(var)) {
            stored = true;
            v = variables.get(var);
        }
        String arg = s.substring(s.indexOf("int(")+4, s.lastIndexOf(","));
        if (l > u) {
            arg = "-" + arg;
            double temp = l;
            l = u;
            u = temp;
        }
        for (double i = l; i < u; i = i + (u-l)/1000000.0) {
            variables.put(var, i);
            double v1 = sort(arg);
            variables.put(var, i + (u-l)/1000000.0);
            double v2 = sort(arg);
            val += (u-l)/1000000.0 * (v2+v1)/2;
        }
        if (stored)
            variables.put(var, v);
        else
            variables.remove(var);
        return val;
    }

    private double differentiate(String s) {
        if (malformed)
            return 0;
        if (s.length() - s.replaceAll(",", "").length() < 2) {
            malformed = true;
            return 0;
        }
        double v = 0;
        boolean stored = false;
        double deg = sort(s.substring(s.lastIndexOf(",")+1, 
                                       s.lastIndexOf(")")));
        s = s.substring(0, s.lastIndexOf(","));
        String var = s.substring(s.lastIndexOf(",")+1);
        String arg = s.substring(s.indexOf("d(")+2, s.lastIndexOf(","));
        if (variables.containsKey(var)) {
            stored = true;
            v = variables.get(var);
        }
        variables.put(var, deg-0.0000001);
        double v1 = sort(arg);
        variables.put(var, deg+0.0000001);
        double v2 = sort(arg);
        if (stored)
            variables.put(var, v);
        else
            variables.remove(var);
        return (v2 - v1) / 0.0000002;
    }

    private double limit(String s) {
        if (malformed)
            return 0;
        if (s.length() - s.replaceAll(",", "").length() < 2) {
            malformed = true;
            return 0;
        }
        double v = 0;
        boolean stored = false;
        double lim = sort(s.substring(s.lastIndexOf(",")+1, 
                                       s.lastIndexOf(")")));
        s = s.substring(0, s.lastIndexOf(","));
        String var = s.substring(s.lastIndexOf(",")+1);
        String arg = s.substring(s.indexOf("limit(")+6, s.lastIndexOf(","));
        if (variables.containsKey(var)) {
            stored = true;
            v = variables.get(var);
        }
        variables.put(var, lim);
        double val = sort(arg);
        if (stored)
            variables.put(var, v);
        else
            variables.remove(var);
        return val;
    }

    private double abs(String s) {
        if (malformed)
            return 0;
        return Math.abs(sort(s.substring(3)));
    }

    private double cos(String s) {
        if (malformed)
            return 0;
        double val;
        if (s.startsWith("cosh")) {
            val = sort(s.substring("cosh".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.cosh(val);
        }
        else if (s.startsWith("cos-1")) {
            val = sort(s.substring("cos-1".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            if (Math.abs(val) > 1) {
                malformed = true;
                return 0;
            }
            return Math.acos(val);
        }
        else {
            val = sort(s.substring("cos".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.cos(val);
        }
    }

    private double sin(String s) {
        if (malformed)
            return 0;
        double val;
        if (s.startsWith("sinh")) {
            val = sort(s.substring("sinh".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.sinh(val);
        }
        else if (s.startsWith("sin-1")) {
            val = sort(s.substring("sin-1".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            if (Math.abs(val) > 1) {
                malformed = true;
                return 0;
            }
            return Math.asin(val);
        }
        else {
            val = sort(s.substring("sin".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.sin(val);
        }
    }

    private double tan(String s) {
        if (malformed)
            return 0;
        double val;
        if (s.startsWith("tanh")) {
            val = sort(s.substring("tanh".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.tanh(val);
        }
        else if (s.startsWith("tan-1")) {
            val = sort(s.substring("tan-1".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.atan(val);
        }
        else {
            val = sort(s.substring("tan".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            int t = (int)(val / (Math.PI/2));
            double test = (Math.PI/2) * t;
            if (test >= val*0.99999 && test <= val*1.00001
                    && test % Math.PI != 0) {
                malformed = true;
                return 0;
            }
            return Math.tan(val);
        }
    }

    private double evaluate(String s) {
        if (malformed)
            return 0;
        double left;
        double right;
        if (isVar(s))
            return variables.get(s);
        else if (isVal(s)) {
            try { return (double)Float.parseFloat(s); }
            catch (NumberFormatException e) {
                malformed = true;
                return 0;
            }
        }
        else if (constants.containsKey(s))
            return constants.get(s);
        else if (s.contains("+")) {
            if (s.startsWith("+") || s.endsWith("+")) {
                malformed = true;
                return 0;
            }
            left = sort(s.substring(0, s.indexOf("+")));
            right = sort(s.substring(s.indexOf("+")+1));
            return left + right;
        }
        else if (s.contains("-") && isVal(s.substring(0, s.indexOf("-")))) {
            if (s.endsWith("-")) {
                malformed = true;
                return 0;
            }
            left = sort(s.substring(0, s.indexOf("-")));
            right = sort(s.substring(s.indexOf("-")+1));
            return left - right;
        }
        else if (s.contains("*")) {
            if (s.startsWith("*") || s.endsWith("*")) {
                malformed = true;
                return 0;
            }
            left = sort(s.substring(0, s.indexOf("*")));
            right = sort(s.substring(s.indexOf("*")+1));
            return left * right;
        }
        else if (s.contains("/")) {
            if (s.startsWith("/") || s.endsWith("/")) {
                malformed = true;
                return 0;
            }
            left = sort(s.substring(0, s.indexOf("/")));
            right = sort(s.substring(s.indexOf("/")+1));
            if (right == 0) {
                malformed = true;
                return 0;
            }
            return left / right;
        }
        else if (s.contains("^")) {
            if (s.startsWith("^") || s.endsWith("^")) {
                malformed = true;
                return 0;
            }
            left = sort(s.substring(0, s.indexOf("^")));
            right = sort(s.substring(s.indexOf("^")+1));
            return Math.pow(left, right);
        }
        else if (s.endsWith("!")) {
            if (s.startsWith("!")) {
                malformed = true;
                return 0;
            }
            left = sort(s.substring(0, s.indexOf("!")));
            for (right = left - 1; right > 1; right--) {
                left *= right;
            }
            return left;
        }
        else if (s.startsWith("abs")) {
            if (s.endsWith("abs")) {
                malformed = true;
                return 0;
            }
            return abs(s);
        }
        else if (s.startsWith("cos")) {
            if (s.endsWith("cos")) {
                malformed = true;
                return 0;
            }
            return cos(s);
        }
        else if (s.startsWith("sin")) {
            if (s.endsWith("sin")) {
                malformed = true;
                return 0;
            }
            return sin(s);
        }
        else if (s.startsWith("tan")) {
            if (s.endsWith("tan")) {
                malformed = true;
                return 0;
            }
            return tan(s);
        }
        else {
            malformed = true;
            return 0;
        }
    }

    private double sort(String s) {
        String left;
        String val;
        String right;
        if (malformed)
            return 0;
        if (s.contains("->")) {
            print = false;
            store(s);
            return 0;
        }
        else if (isBool(s))
            return bool(s);
        else if (s.startsWith("(")) {
            val = subParentheses(s);
            right = s.substring(val.length());
            val = val.substring(val.indexOf("(")+1, val.lastIndexOf(")"));
            return sort(Double.toString(sort(val)) + right);
        }
        else if (s.contains("sqrt(")) {
            if (s.endsWith("sqrt(")) {
                malformed = true;
                return 0;
            }
            left = s.substring(0, s.indexOf("sqrt("));
            val = subParentheses(s.substring(s.indexOf("sqrt(")));
            right = "^(1/2)"+s.substring(s.indexOf("sqrt(") + val.length());
            val = val.replaceFirst("sqrt", "");
            return sort(left + val + right);
        }
        else if (s.contains("log(")) {
            left = s.substring(0, s.indexOf("log("));
            val = subParentheses(s.substring(s.indexOf("log(")));
            right = s.substring(s.indexOf("log(") + val.length());
            return sort(left + Double.toString(log(val)) + right);
        }
        else if (s.contains("ln(")) {
            left = s.substring(0, s.indexOf("ln"));
            val = subParentheses(s.substring(s.indexOf("ln(")));
            right = s.substring(s.indexOf("ln(")+val.length());
            return sort(left + ln(val) + right);
        }
        else if (s.contains("sum(")) {
            left = s.substring(0, s.indexOf("sum("));
            val = subParentheses(s.substring(s.indexOf("sum(")));
            right = s.substring(s.indexOf("sum(") + val.length());
            return sort(left + Double.toString(sum(val, 0)) + right);
        }
        else if (s.contains("prod(")) {
            left = s.substring(0, s.indexOf("prod("));
            val = subParentheses(s.substring(s.indexOf("prod(")));
            right = s.substring(s.indexOf("prod(") + val.length());
            val = val.replace("prod(", "sum(");
            return sort(left + Double.toString(sum(val, 1)) + right);
        }
        else if (s.contains("mod(")) {
            left = s.substring(0, s.indexOf("mod("));
            val = subParentheses(s.substring(s.indexOf("mod(")));
            right = s.substring(s.indexOf("mod(") + val.length());
            return sort(left + Double.toString(mod(val)) + right);
        }
        else if (s.contains("round(")) {
            left = s.substring(0, s.indexOf("round("));
            val = subParentheses(s.substring(s.indexOf("round(")));
            right = s.substring(s.indexOf("round(") + val.length());
            return sort(left + Double.toString(round(val)) + right);
        }
        else if (s.contains("int(")) {
            left = s.substring(0, s.indexOf("int("));
            val = subParentheses(s.substring(s.indexOf("int(")));
            right = s.substring(s.indexOf("int(") + val.length());
            return sort(left + Double.toString(integrate(val)) + right);
        }
        else if (s.contains("d(")) {
            left = s.substring(0, s.indexOf("d("));
            val = subParentheses(s.substring(s.indexOf("d(")));
            right = s.substring(s.indexOf("d(") + val.length());
            return sort(left + Double.toString(differentiate(val)) + right);
        }
        else if (s.contains("limit(")) {
            left = s.substring(0, s.indexOf("limit("));
            val = subParentheses(s.substring(s.indexOf("limit(")));
            right = s.substring(s.indexOf("limit(") + val.length());
            return sort(left + Double.toString(limit(val)) + right);
        }
        else if (s.contains("(")) {
            left = s.substring(0, s.indexOf("("));
            val = subParentheses(s.substring(s.indexOf("(")));
            right = s.substring(s.indexOf("(") + val.length());
            return sort(left + Double.toString(sort(val)) + right);
        }
        else if (s.isEmpty())
            return 0;
        else return evaluate(s);
    }

    private void initConstants() {
        constants = new HashMap<>();
        constants.put("pi", Math.PI);
        constants.put("e", Math.E);
    }

    private String fileReader() throws Exception{
        URL path = Interpreter.class.getResource("InterpreterTest.test");
        File f = new File(path.getFile());
        Scanner scan = new Scanner(new FileReader(f));
        //BufferedReader reader = new BufferedReader(new FileReader(f));
        StringBuilder sb = new StringBuilder();
        //String line = scan.nextLine();
        //String line = reader.readLine();
        while (scan.hasNextLine() /*line != null*/) {
            sb.append(scan.nextLine());
            sb.append("\n");
            //line = reader.readLine();
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        Interpreter interpret = new Interpreter();
        if (args.length > 0 && args[0] != null) {
            String mode = args[0].toLowerCase();
            if (mode.equals("deg"))
                interpret.degrees = true;
            else if (mode.equals("rad"))
                interpret.degrees = false;
            else {
                System.out.println("Unavailable mode");
                System.exit(-1);
            }
        }
        else
            interpret.degrees = false;
        double round = 4.0;
        if (args.length > 1 && args[1] != null) {
            try { round = (double)Float.parseFloat(args[1]); }
            catch (NumberFormatException e) {
                System.out.println("Unavailable round");
                System.exit(-1);
            }
            if (round < 0) {
                System.out.println("Unavailable round");
                System.exit(-1);
            }
        }
        boolean test = false;
        if (args.length > 2 && args[2] != null) {
            if (args[2].equals("1"))
                test = true;
            else if (args[2].equals("0"))
                test = false;
            else {
                System.out.println("Improper testing");
                System.exit(-1);
            }
        }
        interpret.variables = new HashMap<>();
        interpret.initConstants();
        Scanner scan = new Scanner(System.in);
        String in = "";
        try {
            in = interpret.fileReader();
        }
        catch (Exception e) {
            System.out.println("Bad test file");
            System.exit(-1);
        }
        while (true) {
            String input;
            if (!test) {
                input = scan.nextLine();
            }
            else {
                input = in;
                if (in.contains("\n"))
                    in = in.substring(in.indexOf("\n")+1);
                else
                    in = "";
            }
            if (input.isEmpty()) {
                System.exit(0);
            }
            String function = input.toLowerCase().replaceAll(" ", "");
            interpret.malformed = false;
            interpret.print = true;
            if (function.equals("exit"))
                break;
            if (function.equals("clear")) {
                interpret.variables.clear();
                continue;
            }
            interpret.value = interpret.sort(function);
            if (interpret.malformed) {
                System.out.println("Invalid input");
                System.out.println(input);
                continue;
            }
            if (interpret.print) {
                if (interpret.bool) {
                    if (interpret.value == 1)
                        System.out.println("true");
                    else
                        System.out.println("false");
                }
                else {
                    interpret.value = interpret.round("round("+
                                       interpret.value+","+round+")");
                    if (interpret.value % 1 == 0)
                        System.out.println((int)interpret.value);
                    else
                        System.out.println(interpret.value);
                }
            }
        }
    }
}