package vertx;

import vertx.http.Table;

public class PhilosopherProblem {

    public static void main (String [ ] args) {
        Table table = new Table();
        try {
            table.start();
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
    }
}
