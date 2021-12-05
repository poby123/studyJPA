package jpabook.jpashop;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// alt + shfit + o

public class Test {
    public static void main(String... args) {
        // alt
        List<String> arr = new ArrayList<String>();
        try (Scanner scan = new Scanner(System.in)) {
            while (true) {
                String input = scan.next();
                if (input.equals("end")) {
                    break;
                }

                arr.add(input);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("==========");
            for (String str : arr) {
                System.out.println(str);
            }
        }
    }
}
