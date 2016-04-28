import java.util.HashMap;
import java.util.Scanner;

public class Interpreter {

    private HashMap<String, Double> variables;
    private boolean print = false;
    private double value = 0;
    private boolean malformed = false;

    private boolean isVar(String s) {
        return variables.containsKey(s);
    }

    private boolean isNum(String s) {
        try {
            Double.parseDouble(s);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private String formatArg(String s, int args) {
        String arg;
        try {
            arg = s.substring(s.indexOf('(') + 1, s.lastIndexOf(')'));
            int n = args;
            while (n-- != 0)
                arg = arg.substring(0, arg.lastIndexOf(','));
        }
        catch (StringIndexOutOfBoundsException e) {
            malformed = true;
            return "0";
        }
        return arg;
    }

    private double cos(String s) {
        String arg = formatArg(s, 0);
        double val = evaluate(arg);
        if (s.startsWith("cos("))
            return Math.cos(val);
        else if (s.startsWith("cosh("))
            return Math.cosh(val);
        else if (s.startsWith("cos-1("))
            return Math.acos(val);
        else {
            malformed = true;
            return 0;
        }
    }

    private double sin(String s) {
        String arg = formatArg(s, 0);
        double val = evaluate(arg);
        if (s.startsWith("sin("))
            return Math.sin(val);
        else if (s.startsWith("sinh("))
            return Math.sinh(val);
        else if (s.startsWith("sin-1("))
            return Math.asin(val);
        else {
            malformed = true;
            return 0;
        }
    }

    private double tan(String s) {
        String arg = formatArg(s, 0);
        double val = evaluate(arg);
        if (s.startsWith("tan("))
            return Math.tan(val);
        if (s.startsWith("tanh("))
            return Math.tanh(val);
        if (s.startsWith("tan-1"))
            return Math.atan(val);
        else {
            malformed = true;
            return 0;
        }
    }

    private double logarithm(String s) {
        String arg;
        double base;
        if (s.lastIndexOf(',') == -1) {
            arg = formatArg(s, 0);
            base = 10;
        }
        else {
            String args = s.substring(s.lastIndexOf(',')+1, s.lastIndexOf(')'));
            if (args.contains(")")) {
                arg = formatArg(s, 0);
                base = 10;
            }
            else {
                String test = s.substring(s.lastIndexOf(',')+1, s.lastIndexOf(')'));
                base = evaluate(test);
                arg = formatArg(s, 1);
                if (malformed)
                    return 0;
            }
        }
        if (evaluate(arg) == 0 || base == 0) {
            print = false;
            System.out.println("undef");
            return 0;
        }
        if (base == Math.E)
            return Math.log(evaluate(arg));
        else if (base == 1) {
            if (evaluate(arg) != 1 && !malformed) {
                print = false;
                System.out.println("undef");
                return 0;
            }
            else if (malformed)
                return 0;
            else
                return 1;
        }
        else
            return Math.log(evaluate(arg))/Math.log(base);
    }

    private double sum(String s, int n) {
        String arg = formatArg(s, 3);
        String test = s;
        int ubound;
        int lbound;
        String var;
        try {
            ubound = (int)evaluate(test.substring(test.lastIndexOf(',') + 1, test.lastIndexOf(')')));
            test = test.substring(0, test.lastIndexOf(','));
            lbound = (int)evaluate(test.substring(test.lastIndexOf(',') + 1));
            test = test.substring(0, test.lastIndexOf(','));
            var = test.substring(test.lastIndexOf(','));
        }
        catch (StringIndexOutOfBoundsException e) {
            malformed = true;
            return 0;
        }
        double sum = 0;
        boolean contained = variables.containsKey(var);
        double original = 0;
        if (contained)
            original = variables.get(var);
        for (int i = lbound; i <= ubound; i++) {
            variables.put(var, (double)i);
            if (n == 0)
                sum += evaluate(arg);
            else if (n == 1) {
                if (i == lbound)
                    sum = evaluate(arg);
                else
                    sum *= evaluate(arg);
            }
        }
        if (contained)
            variables.put(var, original);
        else
            variables.remove(var);
        return sum;
    }

    private void store(String s) {
        print = false;
        String var = s.substring(0, s.indexOf("->"));
        String arg = s.substring(s.indexOf("->")+2);
        double val = evaluate(arg);
        if (isNum(Character.toString(var.charAt(0))))
            return;
        if (malformed)
            return;
        variables.put(var, val);
    }

    private double arithmetic(String s) {
        if (isNum(s))
            return Double.parseDouble(s);
        else if (isVar(s))
            return variables.get(s);
        else if (s.equals("pi"))
            return Math.PI;
        else if (s.equals("e"))
            return Math.E;
        else if (s.contains(",")) {
            malformed = true;
            return 0;
        }
        return 0;
    }

    private double evaluate(String f) {
        if (f.startsWith("log("))
            return logarithm(f);
        else if (f.startsWith("ln(")) {
            f = f.replaceFirst("ln", "log");
            f = f.substring(0, f.lastIndexOf(')'));
            f = f.concat(",e)");
            return logarithm(f);
        } else if (f.startsWith("cos"))
            return cos(f);
        else if (f.startsWith("sin"))
            return sin(f);
        else if (f.startsWith("tan"))
            return tan(f);
        else if (f.startsWith("sum("))
            return sum(f, 0);
        else if (f.startsWith("prod("))
            return sum(f, 1);
        else if (f.contains("->"))
            store(f);
        else
            return arithmetic(f);
        return 0;
    }

    public static void main(String[] args) {
        Interpreter interpret = new Interpreter();
        interpret.variables = new HashMap<>();
        Scanner scan = new Scanner(System.in);
        while (true) {
            String input = scan.nextLine();
            String function = input.toLowerCase();
            interpret.malformed = false;
            interpret.print = true;
            if (function.equals("exit"))
                break;
            interpret.value = interpret.evaluate(function);
            if (interpret.malformed) {
                System.out.println("Invalid input");
                System.out.println(input);
                continue;
            }
            if (interpret.print) {
                int cast = (int)Math.round(interpret.value * 10000);
                interpret.value = cast / 10000.0;
                if (interpret.value % 1 == 0)
                    System.out.println((int)interpret.value);
                else
                    System.out.println(interpret.value);
            }
        }
    }
}
