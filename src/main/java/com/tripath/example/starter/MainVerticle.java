package com.tripath.example.starter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class MainVerticle extends AbstractVerticle {

  private Long service;

  public static void main(String[] args) {
    Runner.runExample(MainVerticle.class);
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);
    /*
     * these methods aren't necessary for this sample,
     * but you may need them for your projects
     */
    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.PATCH);
    allowedMethods.add(HttpMethod.PUT);

    router.route().handler(CorsHandler.create("*").allowedHeader("*").allowedMethods(allowedMethods));


    // Create a JWT Auth Provider
    JWTAuth jwt = JWTAuth.create(vertx, new JWTAuthOptions(new JsonObject()
      .put("keyStore", new JsonObject()
        .put("type", "jceks")
        .put("path", "keystore.jceks")
        .put("password", "secret"))));

    // protect the API
    router.route("/api/*").handler(JWTAuthHandler.create(jwt, "/api/newToken"));

    // this route is excluded from the auth handler
    router.post("/api/newToken").handler(ctx -> {
      ctx.response().putHeader("Content-Type", "application/json");
      ctx.response().end(jwt.generateToken(new JsonObject(), new JWTOptions().setExpiresInSeconds(6000)));
    });

    // this is the secret API
    router.get("/api/protected").handler(this::getStudent);

    router.route()
      .failureHandler(ctx -> {
         ctx.failure().printStackTrace();
        System.out.println("ERROR EXCEPTION GLOBAL");
        ctx.response().end("ERROR CODE");
      });

    // Serve the non private static pages
    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router).listen(8888);

    service = 1L;
    System.out.println(service);
  }

  public void getStudent(RoutingContext ctx) {
    var student = new Student(1L, "dfsdf");
    System.out.println(service);

    ctx.response().putHeader("Content-Type", "application/json");
    ctx.response().end(JsonObject.mapFrom(student).toString());

  }

  public static class Student implements Serializable {
    Long id;
    String name;

    public Student(Long id, String name) {
      this.id = id;
      this.name = name;
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
