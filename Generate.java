import java.io.FileWriter;
import java.io.IOException;

public class Generate {
    public static void main(String[] args) throws IOException {
        String x = "Hello World!";
        FileWriter writer = new FileWriter("output.txt");

        for (int i = 0; i < 80000000; i++){
            writer.write(x);
            writer.append(((Integer) i).toString());
            writer.append("\n");
        }
        writer.flush();
    }
}
