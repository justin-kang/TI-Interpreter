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

    /*
    //constants
    //store
    //bool
    //four function
    //factorial
    //exponents
    //abs
    //trig
    -logarithms
    -calc
    -summation
    -product
    -rounding
    -mod
    -solver
    -zero
     */

    private boolean isVar(String s) {
        return variables.containsKey(s);
    }

    private boolean isVal(String s) {
        try {
            Double.parseDouble(s);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private double store(String s) {
        if (malformed)
            return 0;
        if (s.indexOf("->") == 0 || s.indexOf("->") == s.length()-1) {
            malformed = true;
            return 0;
        }
        String var = s.substring(0, s.indexOf("->"));
        double val = sort(s.substring(s.indexOf("->")+1));
        variables.put(var, val);
        return 0;
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

    private double bool(String s) {
        if (malformed)
            return 0;
        double left;
        double right;
        if (s.contains(">=")) {
            left = evaluate(s.substring(0, s.indexOf(">=")));
            right = evaluate(s.substring(s.indexOf(">=")+2));
            if (left >= right)
                return 1;
        }
        else if (s.contains("<=")) {
            left = evaluate(s.substring(0, s.indexOf("<=")));
            right = evaluate(s.substring(s.indexOf("<=")+2));
            if (left <= right)
                return 1;
        }
        else if (s.contains(">")) {
            left = evaluate(s.substring(0, s.indexOf(">")));
            right = evaluate(s.substring(s.indexOf(">")+1));
            if (left > right)
                return 1;
        }
        else if (s.contains("<")) {
            left = evaluate(s.substring(0, s.indexOf("<")));
            right = evaluate(s.substring(s.indexOf("<")+1));
            if (left < right)
                return 1;
        }
        else if (s.contains("==")) {
            left = evaluate(s.substring(0, s.indexOf("==")));
            right = evaluate(s.substring(s.indexOf("==")+2));
            if (left == right)
                return 1;
        }
        else if (s.contains("!=")) {
            left = evaluate(s.substring(0, s.indexOf("!=")));
            right = evaluate(s.substring(s.indexOf("!=")+2));
            if (left != right)
                return 1;
        }
        return 0;
    }

    private String group(String s) {
        if (malformed)
            return "";
        int count = 0;
        int index = 0;
        for (char c : s.toCharArray()) {
            if (c == '(')
                count++;
            else if (c == ')')
                count--;
            if (count == 0)
                break;
            index++;
        }
        if (count != 0) {
            malformed = true;
            return "";
        }
        double left = sort(s.substring(1, index));
        String right = s.substring(index);
        return Double.toString(left).concat(right);
    }

    private double abs(String s) {
        if (malformed)
            return 0;
        return Math.abs(evaluate(s));
    }

    private double cos(String s) {
        if (malformed)
            return 0;
        double val;
        if (s.startsWith("cosh")) {
            val = evaluate(s.substring("cosh".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.cosh(val);
        }
        else if (s.startsWith("cos-1")) {
            val = evaluate(s.substring("cos-1".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.acos(val);
        }
        else {
            val = evaluate(s.substring("cos".length()));
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
            val = evaluate(s.substring("sinh".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.sinh(val);
        }
        else if (s.startsWith("sin-1")) {
            val = evaluate(s.substring("sin-1".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.asin(val);
        }
        else {
            val = evaluate(s.substring("sin".length()));
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
            val = evaluate(s.substring("tanh".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.tanh(val);
        }
        else if (s.startsWith("tan-1")) {
            val = evaluate(s.substring("tan-1".length()));
            if (degrees)
                val *= Math.PI / 180.0;
            return Math.atan(val);
        }
        else {
            val = evaluate(s.substring("tan".length()));
            if (degrees)
                val *= Math.PI / 180.0;
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
        else if (isVal(s))
            return Double.parseDouble(s);
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
        else if (s.contains("-")) {
            if (s.endsWith("-")) {
                malformed = true;
                return 0;
            }
            left = sort(s.substring(0, s.indexOf("-")));
            right = sort(s.substring(s.indexOf("-")+1));
            return left - right;
        }
        else if (s.startsWith("*") || s.endsWith("*")) {
            if (s.indexOf("*") == 0) {
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
        else if (s.startsWith("abs"))
            return abs(s);
        else if (s.startsWith("cos"))
            return cos(s);
        else if (s.startsWith("sin"))
            return sin(s);
        else if (s.startsWith("tan"))
            return tan(s);
        else {
            malformed = true;
            return 0;
        }
    }

    private double sort(String s) {
        if (malformed)
            return 0;
        if (s.contains("->")) {
            print = false;
            return store(s);
        }
        else if (isBool(s))
            return bool(s);
        else if (s.startsWith("("))
            return sort(group(s));
        else if (s.contains("(")) {
            String left = s.substring(0, s.indexOf("("));
            String right = group(s.substring(s.indexOf("(")));
            return sort(left.concat(right));
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

    public static void main(String[] args) {
        Interpreter interpret = new Interpreter();
        if (args[0] != null) {
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
        if (args[1] != null) {
            try {
                round = Double.parseDouble(args[1]);
            }
            catch (NumberFormatException e) {
                System.out.println("Unavailable round");
                System.exit(-1);
            }
            if (round < 0) {
                System.out.println("Unavailable round");
                System.exit(-1);
            }
        }
        interpret.variables = new HashMap<>();
        interpret.initConstants();
        Scanner scan = new Scanner(System.in);
        while (true) {
            String input = scan.nextLine();
            String function = input.toLowerCase();
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
                    int cast = (int)Math.round(interpret.value * Math.pow(10.0, round));
                    interpret.value = cast / Math.pow(10.0, round);
                    if (interpret.value % 1 == 0)
                        System.out.println((int)interpret.value);
                    else
                        System.out.println(interpret.value);
                }
            }
        }
    }
}