package com.kingmang.lazurite.editors;

import org.fusesource.jansi.Ansi;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class editor {


    public static void openEditor() {
        Scanner scanner = new Scanner(System.in);
        int lineNumber = 1;
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a("============================================================").reset());
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a("                 Lazurite code editor 0.1").reset());
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a("============================================================").reset());
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a("           -close   - close editor").reset());
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a("           -save    - save the code to the file INDEX.lzr ").reset());
        System.out.println(Ansi.ansi().fg(Ansi.Color.BLUE).a("============================================================").reset());
        StringBuilder code = new StringBuilder();

        while (true) {
            System.out.print(lineNumber + " ~ ");
            //System.out.print(Ansi.ansi().fg(Ansi.Color.BLUE).a(lineNumber + " ~ ").reset());
            String line = scanner.nextLine();

            if ("-close".equals(line)) {
                try {
                    FileWriter writer = new FileWriter("INDEX.lzr");
                    writer.write(code.toString());
                    writer.close();
                    System.out.println("code saved to INDEX.lzr");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
                System.out.print("\033[H\033[2J");
                System.out.flush();
                break;
            } else if ("-save".equals(line)) {
                try {
                    FileWriter writer = new FileWriter("INDEX.lzr");
                    writer.write(code.toString());
                    writer.close();
                    System.out.println("code saved to INDEX.lzr");
                } catch (IOException e) {
                    System.out.println("IOException");
                }
            } else {
                line = line.replaceAll("\\b(if|else|print|println|while|class|new|for)\\b", Ansi.ansi().fg(Ansi.Color.RED).a("$1").reset().toString());
                line = line.replaceAll("\\b(return|func)\\b", Ansi.ansi().fg(Ansi.Color.BLUE).a("$1").reset().toString());
                line = line.replaceAll("//.*", Ansi.ansi().fg(Ansi.Color.GREEN).a("$0").reset().toString());

                System.out.println(line);

                code.append(line).append("\n");
                lineNumber++;
            }
        }
    }
}