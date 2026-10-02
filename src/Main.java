import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<String> argList = new ArrayList<>();
        boolean interactive = false;
        boolean helpRequested = false;

        for (String arg : args) {
            if (arg.equals("-i")) {
                interactive = true;
            } else if (arg.equals("-h") || arg.equals("--help")) {
                helpRequested = true;
            } else {
                argList.add(arg);
            }
        }

        if (helpRequested) {
            printHelp();
            return;
        }

        int exitCode;
        if (interactive) {
            new InteractiveConsole().run();
            exitCode = 0;
        } else {
            exitCode = new CommandLineProcessor().run(argList.toArray(new String[0]));
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static void printHelp() {
        System.out.println("Использование:");
        System.out.println("  java -jar lab1.jar -i");
        System.out.println("      Запуск в интерактивном режиме.");
        System.out.println();
        System.out.println("  java -jar lab1.jar [<алгоритм>] -f <путь1> [<путь2> ...]");
        System.out.println("      Запуск в неинтерактивном режиме.");
        System.out.println();
        System.out.println("Алгоритмы (выбирается ровно один; по умолчанию -sha256):");
        System.out.println("  -md5");
        System.out.println("  -sha256");
        System.out.println("  -blake2b256");
        System.out.println("  -blake2s256");
        System.out.println("  -shake256 [bits]      bits - длина вывода в битах, кратна 8 (по умолчанию 256)");
    }
}