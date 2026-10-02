import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class InteractiveConsole {

    private final Scanner scanner;

    public InteractiveConsole() {
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Интерактивный режим вычисления хэш-значений файлов.");
        System.out.println("Введите 'exit' или 'quit' в любой момент, чтобы выйти.");
        System.out.println();

        while (true) {
            HashUtil.Algorithm algorithm = askAlgorithm();
            if (algorithm == null) {
                break;
            }

            int shakeBits = HashUtil.DEFAULT_SHAKE256_BITS;
            if (algorithm == HashUtil.Algorithm.SHAKE256) {
                Integer bits = askShakeBits();
                if (bits == null) {
                    break;
                }
                shakeBits = bits;
            }

            String filePathStr = askFilePath();
            if (filePathStr == null) {
                break;
            }

            Path path = Paths.get(filePathStr);
            try {
                String hex = HashUtil.computeHash(algorithm, path, shakeBits);
                String label = algorithm == HashUtil.Algorithm.SHAKE256
                        ? algorithm.getDisplayName() + "/" + shakeBits
                        : algorithm.getDisplayName();
                System.out.println(label + "(" + filePathStr + ") = " + hex);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
            System.out.println();
        }

        System.out.println("Завершение работы.");
    }

    private HashUtil.Algorithm askAlgorithm() {
        while (true) {
            System.out.println("Выберите алгоритм хэширования:");
            System.out.println("  1) MD5");
            System.out.println("  2) SHA-256");
            System.out.println("  3) BLAKE2b-256");
            System.out.println("  4) BLAKE2s-256");
            System.out.println("  5) SHAKE-256");
            System.out.print("> ");

            if (!scanner.hasNextLine()) {
                return null;
            }
            String line = scanner.nextLine().trim();

            if (isExitCommand(line)) {
                return null;
            }

            switch (line) {
                case "1":
                case "md5":
                    return HashUtil.Algorithm.MD5;
                case "2":
                case "sha256":
                case "sha-256":
                    return HashUtil.Algorithm.SHA256;
                case "3":
                case "blake2b256":
                case "blake2b-256":
                    return HashUtil.Algorithm.BLAKE2B256;
                case "4":
                case "blake2s256":
                case "blake2s-256":
                    return HashUtil.Algorithm.BLAKE2S256;
                case "5":
                case "shake256":
                case "shake-256":
                    return HashUtil.Algorithm.SHAKE256;
                default:
                    System.out.println("Не удалось распознать выбор: \"" + line + "\". Попробуйте снова.");
            }
        }
    }

    private Integer askShakeBits() {
        while (true) {
            System.out.print("Введите длину вывода SHAKE-256 в битах (кратно 8, Enter — по умолчанию "
                    + HashUtil.DEFAULT_SHAKE256_BITS + "): ");
            if (!scanner.hasNextLine()) {
                return null;
            }
            String line = scanner.nextLine().trim();

            if (isExitCommand(line)) {
                return null;
            }
            if (line.isEmpty()) {
                return HashUtil.DEFAULT_SHAKE256_BITS;
            }
            try {
                int bits = Integer.parseInt(line);
                if (bits > 0 && bits % 8 == 0) {
                    return bits;
                }
                System.out.println("Значение должно быть положительным и кратным 8.");
            } catch (NumberFormatException e) {
                System.out.println("Не удалось распознать число: \"" + line + "\".");
            }
        }
    }

    private String askFilePath() {
        System.out.print("Введите путь к файлу: ");
        if (!scanner.hasNextLine()) {
            return null;
        }
        String line = scanner.nextLine().trim();
        if (isExitCommand(line)) {
            return null;
        }
        return line;
    }

    private boolean isExitCommand(String line) {
        String normalized = line.trim().toLowerCase();
        return normalized.equals("exit") || normalized.equals("quit") || normalized.equals("выход");
    }
}