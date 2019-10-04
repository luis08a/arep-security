package sec;

import static spark.Spark.*;

import spark.ModelAndView;

import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        //secure("deploy/keystore.jks", "password", null, null);
        get("/secureHello", (req, res) -> "Hello Secure World");


        get("/register", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new VelocityTemplateEngine().render(
                new ModelAndView(model, "templates/register.html")
            );
        });

        post("/register", (req, res) -> {
            String u = req.queryParams("user");
            String p = req.queryParams("pwd");
            Map<String, Object> model = new HashMap<>();
            model.put("user",u);
            model.put("pwd",p);
            return new VelocityTemplateEngine().render(
                new ModelAndView(model, "templates/home.vm")
            );
        });


        get("login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new VelocityTemplateEngine().render(
                new ModelAndView(model, "templates/login.html")
            );
        });
    }
}
