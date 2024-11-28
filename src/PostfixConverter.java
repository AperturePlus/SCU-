import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class PostfixConverter extends JFrame {
    private static final Map<String, Integer> OPERATOR_PRECEDENCE = new HashMap<>();

    static {
        OPERATOR_PRECEDENCE.put("+", 1);
        OPERATOR_PRECEDENCE.put("-", 1);
        OPERATOR_PRECEDENCE.put("*", 2);
        OPERATOR_PRECEDENCE.put("/", 2);
        OPERATOR_PRECEDENCE.put("%", 3);
        OPERATOR_PRECEDENCE.put("^", 4);
        OPERATOR_PRECEDENCE.put("&", 4);
        OPERATOR_PRECEDENCE.put("sqrt", 4);
    }

    private static final Map<Character, Character> FULL_TO_HALF = new HashMap<>();

    static {
        FULL_TO_HALF.put('＋', '+');
        FULL_TO_HALF.put('－', '-');
        FULL_TO_HALF.put('＊', '*');
        FULL_TO_HALF.put('／', '/');
        FULL_TO_HALF.put('（', '(');
        FULL_TO_HALF.put('）', ')');
        FULL_TO_HALF.put('％', '%');
        FULL_TO_HALF.put('＾', '^');
        FULL_TO_HALF.put('＆', '&');
    }

    private static final Set<String> FUNCTIONS = new HashSet<>(List.of("sqrt"));

    private static String full2Half(String expression) {
        StringBuilder sb = new StringBuilder();
        for (char c : expression.toCharArray()) {
            if (FULL_TO_HALF.containsKey(c))
                sb.append(FULL_TO_HALF.get(c));
            else sb.append(c);
        }
        return sb.toString();
    }

    public static List<String> infix2Postfix(String expression) {
        expression = full2Half(expression);
        List<String> output = new ArrayList<>();
        Stack<String> operators = new Stack<>();
        boolean lastWasOperator = true; // Track if last token was an operator or start
        boolean lastWasRightParenthesis = false;

        String[] tokens = expression.split("(?<=[-+*/%^&()])|(?=[-+*/%^&()])");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty()) continue;

            if (isNumber(token)) {
                if (lastWasRightParenthesis) {
                    throw new IllegalArgumentException("Syntax error: Missing operator between ')' and a number.");
                }
                output.add(token);
                lastWasOperator = false;
                lastWasRightParenthesis = false;
            } else if (isFunction(token)) {
                if (lastWasRightParenthesis) {
                    throw new IllegalArgumentException("Syntax error: Missing operator between ')' and a function.");
                }
                operators.push(token);
                lastWasOperator = true;
                lastWasRightParenthesis = false;
            } else if (isOperator(token)) {
                if (token.matches("[*/%^&]") && (lastWasOperator || i == 0)) {
                    throw new IllegalArgumentException("Syntax error: Operator '" + token + "' cannot be at the start or follow another operator.");
                }
                while (!operators.isEmpty() && isOperator(operators.peek()) &&
                        (OPERATOR_PRECEDENCE.get(token) <= OPERATOR_PRECEDENCE.get(operators.peek()))) {
                    output.add(operators.pop());
                }
                operators.push(token);
                lastWasOperator = true;
                lastWasRightParenthesis = false;
            } else if (token.equals("-")) {
                if (i == 0 || lastWasOperator || tokens[i - 1].equals("(")) {
                    output.add("0"); // Add zero as a placeholder for unary negation
                }
                operators.push(token);
                lastWasOperator = true;
                lastWasRightParenthesis = false;
            } else if (token.equals("(")) {
                if (!lastWasOperator && !lastWasRightParenthesis) {
                    throw new IllegalArgumentException("Syntax error: Missing operator before '('.");
                }
                operators.push(token);
                lastWasOperator = true;
                lastWasRightParenthesis = false;
            } else if (token.equals(")")) {
                if (lastWasOperator) {
                    throw new IllegalArgumentException("Syntax error: Empty parentheses or missing operand/operator.");
                }
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                if (operators.isEmpty()) {
                    throw new IllegalArgumentException("Syntax error: Mismatched parentheses.");
                }
                operators.pop(); // Pop the '('
                if (!operators.isEmpty() && isFunction(operators.peek())) {
                    output.add(operators.pop());
                }
                lastWasOperator = false;
                lastWasRightParenthesis = true;

                // Check if next token after ')' is a number or '(' without an operator in between
                if (i + 1 < tokens.length && (isNumber(tokens[i + 1]) || tokens[i + 1].equals("("))) {
                    throw new IllegalArgumentException("Syntax error: Missing operator between ')' and next operand or '('.");
                }
            } else {
                throw new IllegalArgumentException("Invalid character in expression: " + token);
            }
        }

        if (lastWasOperator) {
            throw new IllegalArgumentException("Syntax error: Expression cannot end with an operator.");
        }

        while (!operators.isEmpty()) {
            String op = operators.pop();
            if (op.equals("(")) {
                throw new IllegalArgumentException("Syntax error: Mismatched parentheses.");
            }
            output.add(op);
        }
        return output;
    }


    private static boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isFunction(String token) {
        return FUNCTIONS.contains(token);
    }

    private static boolean isOperator(String token) {
        return OPERATOR_PRECEDENCE.containsKey(token);
    }

    public PostfixConverter() {
        // 窗口、容器设置
        setTitle("Calculator");
        setSize(350, 490);
        setLocation(500, 250);
        Container c = getContentPane();
        c.setLayout(null);

        // 菜单事件设置-切换模式
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("切换模式");
        JMenuItem mi1 = new JMenuItem("表达式求值");
        JMenuItem mi2 = new JMenuItem("计算器求值");
        m1.add(mi1);
        m1.add(mi2);
        mb.add(m1);
        setJMenuBar(mb);

        // 输入面板
        JTextArea jt = new JTextArea(20, 20);
        jt.setFont(new Font("Aria", Font.BOLD, 30));
        jt.setLineWrap(true);
        JScrollPane sp = new JScrollPane(jt);
        jt.setCaretPosition(jt.getDocument().getLength());
        sp.setBounds(0, 0, 338, 150);
        c.add(sp);

        // 按键面板
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1, 3, 1, 1));
        p.setBounds(0, 160, 340, 40);

        // 按键设置
        String[] num = {"清空", "删除", "结果"};
        JButton[] jb = new JButton[num.length];
        for (int i = 0; i < num.length; i++) {
            jb[i] = new JButton(num[i]);
            p.add(jb[i]);
        }
        c.add(p);

        // 按钮事件监听方法
        jb[0].addActionListener(e -> jt.setText("")); // 监听 清空 按键

        jb[1].addActionListener(e -> { // 监听 删除 按键
            if (!jt.getText().isEmpty()) {
                jt.setText(jt.getText().substring(0, jt.getText().length() - 1));
            }
        });

        jb[2].addActionListener(e -> { // 监听 结果 按键
            try {
                System.out.println(jt.getText());
                double x = PostfixEvaluator.evaluate(infix2Postfix(jt.getText()));
                jt.setText("");                  // 清空输入框
                jt.append(String.valueOf(x));    // 显示计算结果
            } catch (IllegalArgumentException ex) { // 捕获自定义异常
                jt.setText("表达式错误: " + ex.getMessage());
            } catch (Exception ex) {             // 捕获其他异常
                jt.setText("计算错误!");
            }
        });

        // 注释面板
        JPanel p1 = new JPanel();
        p1.setBounds(0, 210, 340, 200);
        JTextArea jt2 = new JTextArea();
        jt2.setEditable(false);
        jt2.setFont(new Font("Aria", Font.BOLD, 20));
        jt2.setText("  键盘输入表达式，\n"
                + "  表达式可包含加(+)、减(-)、\n"
                + "  乘(*)、除(/)、求模(%)、\n"
                + "  平方根(sqrt())、\n"
                + "  开方(&)和乘方(^) 运算，\n"
                + "  并能使用括号()。\n"
                + "  左上角切换计算器模式！\n");
        p1.add(jt2);
        c.add(p1);

        // 计算机按键面板
        JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(6, 4, 1, 1));
        p2.setBounds(0, 155, 340, 280);

        // 放置按钮
        String[] num2 = {
                "C", "(", ")", "/",
                "&", "^", "%", "sqrt(",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "0", ".", "DEL", "="
        };
        JButton[] jb2 = new JButton[num2.length];
        for (int i = 0; i < num2.length; i++) {
            jb2[i] = new JButton(num2[i]);
            p2.add(jb2[i]);
        }
        c.add(p2);

        // 按键输入处理
        for (int i = 1; i < 22; i++) {
            final int j = i;
            jb2[i].addActionListener(e -> jt.append(num2[j]));
        }

        // 监听 C 按键
        jb2[0].addActionListener(e -> jt.setText(""));

        // 监听 DEL 按键
        jb2[22].addActionListener(e -> {
            if (!jt.getText().isEmpty()) {
                jt.setText(jt.getText().substring(0, jt.getText().length() - 1));
            }
        });

        // 监听 = 按键
        jb2[23].addActionListener(e -> {
            try {
            System.out.println(jt.getText());
            double x = PostfixEvaluator.evaluate(infix2Postfix(jt.getText()));
            jt.setText("");                  // 清空输入框
            jt.append(String.valueOf(x));    // 显示计算结果
        } catch (IllegalArgumentException ex) { // 捕获自定义异常
            jt.setText("表达式错误: " + ex.getMessage());
        } catch (Exception ex) {             // 捕获其他异常
            jt.setText("计算错误!");
        }
        });

        p2.setVisible(false);

        // 设置 Enter 为 “=”
        KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
        jt.getInputMap().put(enter, "none");
        this.getRootPane().setDefaultButton(jb[2]);

        mi2.addActionListener(e -> {
            p.setVisible(false);
            p2.setVisible(true);
            p1.setVisible(false);
            this.getRootPane().setDefaultButton(jb2[23]);
        });
        mi1.addActionListener(e -> {
            p.setVisible(true);
            p2.setVisible(false);
            p1.setVisible(true);
            this.getRootPane().setDefaultButton(jb[2]);
        });

        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    // 表达式求值方法
    private void handleExpressionEvaluation(JTextArea jt) {
        try {
            System.out.println(jt.getText());
            double x = PostfixEvaluator.evaluate(infix2Postfix(jt.getText()));
            jt.setText(""); // 清空输入框
            jt.append(String.valueOf(x)); // 显示计算结果
        } catch (Exception ex) { // 异常情况
            jt.setText(ex.getMessage() != null ? ex.getMessage() : "计算错误!");
        }
    }
}
