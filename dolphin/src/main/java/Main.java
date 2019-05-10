import static spark.Spark.get;
import static spark.Spark.port;

public class Main {

    public static void main(String[] args) {
        System.out.println("hello world");
        port(8089);
        get("hello", (req, res) -> ("Hello World"));
    }
}
