package vertx.http;

import com.google.common.collect.Lists;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import vertx.consumer.Philosopher;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Table extends AbstractVerticle {

    private Vertx vertx;

    private int forksAvailable;
    private List<Philosopher> philosophers;

    public Table() {
        this.vertx = Vertx.vertx();
        this.philosophers = Lists.newArrayList();
        this.forksAvailable = 0;
    }

    public int getForksAvailable() {
        return forksAvailable;
    }

    public Predicate<Philosopher> getExistPhilosopher(String name) {
        return philosopher -> name.equals(philosopher.getName());
    }

    @Override
    public void start() throws Exception {
        System.out.println("Starting the Table...");
        Router router = Router.router(vertx);

        router.get("/").handler(this::showTable);

        router.get("/add/:name").handler(this::addPhilosopher);
        router.get("/delete/:name").handler(this::deletePhilosopher);

        router.get("/end").handler(this::endProcess);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void endProcess(RoutingContext rc) {
        JsonObject json = new JsonObject().put("message", "finishing the process");
        rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(json.encode());

        vertx.deploymentIDs().forEach(vertx::undeploy);
        vertx.close();
        System.out.println("Table closed");
    }

    private void showTable(RoutingContext rc) {
        JsonObject json = this.getJsonInformation();
        rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(json.encode());
    }

    private void addPhilosopher(RoutingContext rc) {
        String name = this.getParam(rc,"name");

        JsonObject json = null;
        if (this.philosophers.stream().anyMatch(getExistPhilosopher(name))) {
            json = this.getJsonInformation();
            System.out.println(name + " is already on the table");
        } else {
            Philosopher philosopher = new Philosopher(name);
            this.philosophers.add(philosopher);

            this.forksAvailable++;
            json = this.getJsonInformation().put("newPhilosopher", philosopher.toString());
            System.out.println(name + " is joining to the table");
        }

        rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(json.encode());
    }

    private void deletePhilosopher(RoutingContext rc) {
        String name = this.getParam(rc,"name");
        JsonObject json = null;

        List<Philosopher> target = this.philosophers.stream().filter(getExistPhilosopher(name)).collect(Collectors.toList());
        if (!target.isEmpty()) {
            this.philosophers.removeAll(target);

            this.forksAvailable--;
            json = this.getJsonInformation().put("deletedPhilosopher", name);
            System.out.println(name + " has left the table");
        } else {
            json = this.getJsonInformation();
            System.out.println("No one has left the table");
        }

        rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(json.encode());
    }

    private JsonObject getJsonInformation() {
        JsonArray jsonPhilosophers = new JsonArray();
        if (!this.philosophers.isEmpty()) {
            this.philosophers.stream()
                    .forEach(philosopher -> {
                        jsonPhilosophers.add(philosopher.toString());
                    });
        }

        return new JsonObject()
                .put("forks", this.philosophers.size())
                .put("forksAvailable", this.getForksAvailable())
                .put("philosophers", jsonPhilosophers);
    }

    private String getParam(RoutingContext rc, String param) {
        return rc.pathParam(param) == null ? "" : rc.pathParam(param);
    }
}
