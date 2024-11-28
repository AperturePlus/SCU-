public class Main {

    public static void main(String[] args) {
/*      String expr;
        System.out.println("*****This is a calculator program*****");
        System.out.print("Please enter the expression:");
        Scanner sc = new Scanner(System.in);
        try{
            while (sc.hasNextLine()) {
                expr = sc.nextLine();
                if(expr.equals("exit")){
                    System.out.println("Program terminated.");
                    break;
                }
                List<String> postfix = PostfixConverter.infix2Postfix(expr);
                System.out.println("Postfix notation: " + postfix);
                System.out.println("Result: " + PostfixEvaluator.evaluate(postfix));
                System.out.print("Please enter the expression:");
            }
        }catch (Exception e){
            System.out.println(e + ". Invalid expression");
        }
*/
        new PostfixConverter();
    }
}