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
    //sqrt
    //abs
    //trig
    -logarithms
    -differentiation
    -integration
    -limits
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
            Float.parseFloat(s);
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

    private String group(String s) {
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
            if (count == 0 && set)
                break;
            index++;
        }
        if (count != 0) {
            malformed = true;
            return "";
        }
        double left = sort(s.substring(1, index));
        String right = s.substring(index+1);
        return Double.toString(left) + right;
    }

    private String subParentheses(String s) {
        int count = 0;
        int index = 0;
        boolean set = false;
        for (char c : s.toCharArray()) {
            if (c == '(') {
                count++;
                set = true;
            }
            else if (c == ')') {
                count--;
            }
            index++;
            if (count == 0 && set)
                break;
        }
        if (index == s.length())
            return s;
        return s.substring(0, index);
    }

    private String log(String s) {
        String left = s.substring(0, s.indexOf("log("));
        String val = subParentheses(s.substring(s.indexOf("log(")));
        String right = s.substring(s.indexOf("log(")+val.length());
        double arg;
        if (val.contains(",")) {
            arg = sort(val.substring(val.indexOf("(")+1, val.lastIndexOf(",")));
            double base = sort(val.substring(val.lastIndexOf(",")+1, val.lastIndexOf(")")));
            if (arg == 0) {
                malformed = true;
                return "";
            }
            arg = Math.log(arg)/Math.log(base);
        }
        else {
            arg = sort(val.substring(val.indexOf("(")+1, val.lastIndexOf(")")));
            if (arg == 0) {
                malformed = true;
                return "";
            }
            arg = Math.log10(arg);
        }
        if (Double.isNaN(arg)) {
            malformed = true;
            return "";
        }
        val = Double.toString(arg);
        return left + val + right;
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
            if (test >= val*0.999 && test <= val*1.001
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
            try {
                return (double)Float.parseFloat(s);
            }
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
        else if (s.contains("sqrt(")) {
            if (s.endsWith("sqrt(")) {
                malformed = true;
                return 0;
            }
            int index = s.indexOf("sqrt(");
            String test = s.substring(index);
            int count = 0;
            boolean start = false;
            for (char c : test.toCharArray()) {
                if (c == '(') {
                    count++;
                    start = true;
                }
                else if (c == ')')
                    count--;
                index++;
                if (count == 0 && start)
                    break;
            }
            String left = s.substring(0, s.indexOf("sqrt("));
            String val;
            String right;
            if (index == s.length()) {
                val = s.substring(s.indexOf("sqrt")+4);
                right = "^(1/2)";
            }
            else {
                val = s.substring(s.indexOf("sqrt")+4, index);
                right = "^(1/2)" + s.substring(index);
            }
            return sort(left + val + right);
        }
        else if (s.contains("log(")) {
            return sort(log(s));
        }
        else if (s.contains("ln(")) {
            return 0;
        }
        else if (s.contains("sum(")) {
            return 0;
        }
        else if (s.contains("prod(")) {
            return 0;
        }
        else if (s.contains("mod(")) {
            return 0;
        }
        else if (s.contains("round(")) {
            return 0;
        }
        else if (s.contains("d(")) {
            return 0;
        }
        else if (s.contains("int(")) {
            return 0;
        }
        else if (s.contains("limit(")) {
            return 0;
        }
        else if (s.contains("(")) {
            String left = s.substring(0, s.indexOf("("));
            String right = group(s.substring(s.indexOf("(")));
            return sort(left + right);
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
            try {
                round = (double)Float.parseFloat(args[1]);
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
                    double f = Math.pow(10.0, round);
                    int cast = (int)Math.round(interpret.value * f);
                    interpret.value = cast / f;
                    if (interpret.value % 1 == 0)
                        System.out.println((int)interpret.value);
                    else
                        System.out.println(interpret.value);
                }
            }
        }
    }
}
