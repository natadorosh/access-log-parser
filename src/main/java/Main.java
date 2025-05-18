import java.util.Scanner;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("Введите первое число:");
        int number1 = new Scanner(System.in).nextInt();
        System.out.println("Введите второе число:");
        int number2 = new Scanner(System.in).nextInt();
        System.out.println("Сумма:");
        int firstNumber = 5;
        int secondNumber = 10;
        double quotient = (double) firstNumber/secondNumber;
        System.out.println(firstNumber+secondNumber);
        System.out.println("Разница:");
        System.out.println(firstNumber-secondNumber);
        System.out.println("Произведение:");
        System.out.println(firstNumber*secondNumber);
        System.out.println("Частное:");
        System.out.println(quotient);
    }
}
