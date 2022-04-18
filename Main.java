import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static String readLastNBytes(String fileName, int bytes) throws IOException {
        return readLastNBytes(new File(fileName), bytes);
    }

    /*
    Метод читает помледние bytes байтов из файла и озвращает их
    Аналогично можно сделать и с чтением из любого места в файле, если знать,
    сколько символов до нужного места
    Можно прочитать и "тупо" с середины, поделив длину на 2
     */
    /*
    Проблем с чтением больших файлов нет, т.к. сразу весь файл в буфер не загружается,
    а загружается только по запросу
    В принципе применимо для всех наследников классов Reader и InputStream (FileInputStream, FileReader,
    BufferedReader)
     */
    public static String readLastNBytes(File file, int bytes) throws IOException {
        /*
        Узнаём длину файла в байтах, чтобы рассчитать то количество,
        которое нужно пропустить
         */
        long len = file.length();

        if (bytes > len){
            return null;
        }
        /*
        Создаем новый InputStream
        Пропускаем нужное количество байтов
         */
        FileInputStream fis = new FileInputStream(file);
        fis.skip(len - bytes);

        /*
        Создаём массив байтов и считываем туда конец файла
         */
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);

        /*
        Преобразуем считанное в String
        и возвращаем
         */
        StringBuilder res = new StringBuilder();

        for (byte b: buffer){
            res.append((char) b);
        }

        return res.toString();
    }

    /*
    Scanner удобен для чтения входных потоков при использование разделителя (например перенос строки)
    Не загружает весь файл в память, поэтому при чтении больших файлов проблем нет
    (не учитывая физические ограничения памяти и процессора),
    просто читаем такими порциями,
    какими нам надо (имеется в виду с учетом разделителя)
    Удобно прочитать с конца файла нельзя, т.к. метод пропуска (skip) работает только с регулярными выражениями
    Можно сохранять ввод в буфер нужного размера, а когда файл закончится работать с получившимся буфером,
    но возникает проблема - необходимо "тупо" прочитать весь файл
     */
    public static void readUsingScanner(String fileName, int lines) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(fileName));
        while (sc.hasNext() && lines > 0){
            System.out.println(sc.nextLine());
            lines--;
        }
    }

    /*
    nio.file.Files умеет читать файл только целиком, поэтому для работы с большими файлами не подходит
     */
    public static void readUsingFiles(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        byte[] bytes = Files.readAllBytes(path);
    }

    /*
    С помощью FileChannel можно быстро (очень быстро, по сравнению с классами для чтения из io)
    считывать данные из файла порциями в буффер. В данном случае размер буффера 1024 байта, т.к. файл слишком большой,
    если файл относительно маленький, то можно задать буфферу размер равный размеру файла
    Читать целенаправленно с конца файла нельзя
    Отлично подходит для чтения больших файлов (например ~2 Гб файл читается полность за ~8 секунд (на моём компьютере),
    а от Scanner'а конца чтения я не дождался)
     */
    public static void readUsingFileChannel(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        FileChannel inChannel = fis.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (inChannel.read(buffer) > 0) {
            buffer.flip(); // Устанавливает limit на текущее значение поля position, position обнуляет
            for (int i = 0; i < buffer.limit(); i++) {
                // Полученные значения не выводил, т.к. вывод в консоль сам по себе долгий
                buffer.get();
            }
            buffer.clear(); // обнуляет position, приравнивает limit к capacity
        }
    }

    /*
    Ну и наконец чтение нужного кол-ва строк с конца файла
    RandomAccessFile позволяет свободно перемещаться по файлу, имеет указатель на текущее местоположение
     */
    public static String readLastNLines(String fileName, int lines) throws IOException {
        File file = new File(fileName);
        StringBuilder builder = new StringBuilder();

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        long pos = file.length() - 1; // Получаем индекс последнего символа в файле

        // Перебираем символы файла с конца до тех пор, пока не встретим нужное кол-во переносов
        for (long i = pos; i >= 0; i--){
            // Перемещаем указатель в конец и считываем символ
            randomAccessFile.seek(i);
            char c = (char) randomAccessFile.read();

            if (c == '\n'){
                if (--lines == 0){
                    break;
                }
            }

            builder.append(c);
        }

        // "Переворачиваем результат и возвращаем строку
        return builder.reverse().toString();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String path = "output.txt";

        // Читаем последние 200 байт
        System.out.println(readLastNBytes(path, 200));

        // Читаем последние 10 строк
        System.out.println(readLastNLines(path, 10));

        // Читаем весь файл (долго, но терпимо для большого файла)
        readUsingFileChannel(path);

        // Первые 10 строк Scanner'ом (весь файл сразу читать больно)
        readUsingScanner(path, 10);

        // Читаем весь файл с помощью Files (с большим файлом на этом этапе всё сломается)
        readUsingFiles(path);
    }
}
