import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommandLineProcessor {

    public int run(String[] args) {
        HashUtil.Algorithm algorithm = null;
        int shakeBits = HashUtil.DEFAULT_SHAKE256_BITS;
        List<String> filePaths = new ArrayList<>();

        int i = 0;
        while (i < args.length) {
            String arg = args[i];

            if (arg.equals("-h") || arg.equals("--help")) {
                Main.printHelp();
                return 0;
            }

            HashUtil.Algorithm parsed = HashUtil.parseAlgorithmFlag(arg);
            if (parsed != null) {
                if (algorithm != null) {
                    System.err.println("Ошибка: указано более одного алгоритма. " +
                            "В неинтерактивном режиме допускается ровно один алгоритм.");
                    return 1;
                }
                algorithm = parsed;

                if (parsed == HashUtil.Algorithm.SHAKE256) {
                    if (i + 1 < args.length && isInteger(args[i + 1])) {
                        i++;
                        shakeBits = Integer.parseInt(args[i]);
                        if (shakeBits <= 0 || shakeBits % 8 != 0) {
                            System.err.println("Ошибка: длина вывода SHAKE-256 должна быть " +
                                    "положительным числом, кратным 8. Получено: " + shakeBits);
                            return 1;
                        }
                    }
                }
                i++;
                continue;
            }

            if (arg.equals("-f")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Ошибка: после флага -f должен следовать хотя бы один путь к файлу.");
                    return 1;
                }
                while (i < args.length && !isKnownFlag(args[i])) {
                    filePaths.add(args[i]);
                    i++;
                }
                continue;
            }

            System.err.println("Неизвестный параметр: " + arg);
            Main.printHelp();
            return 1;
        }

        if (algorithm == null) {
            algorithm = HashUtil.Algorithm.SHA256;
        }

        if (filePaths.isEmpty()) {
            System.err.println("Ошибка: не указан ни один файл. Используйте флаг -f <путь1> <путь2> ...");
            return 1;
        }

        return computeAndPrint(algorithm, shakeBits, filePaths);
    }

    private int computeAndPrint(HashUtil.Algorithm algorithm, int shakeBits, List<String> filePaths) {
        int exitCode = 0;
        for (String rawPath : filePaths) {
            Path path = Paths.get(rawPath);
            try {
                String hex = HashUtil.computeHash(algorithm, path, shakeBits);
                System.out.println(formatAlgorithmLabel(algorithm, shakeBits) + "  " + rawPath + "  " + hex);
            } catch (Exception e) {
                System.err.println("Ошибка при обработке файла \"" + rawPath + "\": " + e.getMessage());
                exitCode = 1;
            }
        }
        return exitCode;
    }

    private static String formatAlgorithmLabel(HashUtil.Algorithm algorithm, int shakeBits) {
        if (algorithm == HashUtil.Algorithm.SHAKE256) {
            return algorithm.getDisplayName() + "/" + shakeBits;
        }
        return algorithm.getDisplayName();
    }

    private static boolean isInteger(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int j = 0; j < s.length(); j++) {
            if (!Character.isDigit(s.charAt(j))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isKnownFlag(String s) {
        return s.equals("-f") || s.equals("-h") || s.equals("--help") || s.equals("-i")
                || HashUtil.parseAlgorithmFlag(s) != null;
    }
}