package sec;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.secure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.google.common.hash.Hashing;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

/**
 * Hello world!
 *
 */
public class App {
    private static final String STORE_FILE_PATH = "/src/main/resources/store.txt";

    public static void main(String[] args) {
        secure("secure/keystore.jks", "password", null, null);
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/home.vm"));
        });

        get("/auth/register", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/register.html"));
        });

        post("/auth/register", (req, res) -> {
            String u = req.queryParams("user");
            String p = req.queryParams("pwd");
            p = encrypt(p);
            createUser(u, p);
            Map<String, Object> model = new HashMap<>();
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/login.html"));
        });

        get("/auth/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/login.html"));
        });

        post("/auth/login", (req, res) -> {
            String u = req.queryParams("user");
            String p = req.queryParams("pwd");
            p = encrypt(p);
            if (login(u, p)) {
                req.session().attribute("connect", "connected");
                Map<String, Object> model = new HashMap<>();
                model.put("user", u);
                model.put("pwd", p);
                return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/home.vm"));
            }
            return "Error";
        });

        get("/logout", (req,res)->{
            return "Ha cerrado sesión";
        });

        after("/logout",(req,res)->{
            req.session().invalidate();
            req.session().removeAttribute("connect");
        });

        before("/auth/*", (req, res) -> {
            req.session().attribute("connect", "temp");
        });

        after("/auth/*", (req, res) -> {
            String auth = req.session().attribute("connect");
            if (auth != null && auth.equals("temp")) {
                req.session().removeAttribute("connect");
            }
        });

        before((request, response) -> {
            String auth = request.session().attribute("connect");
            if (request.session().isNew() || auth == null) {
                response.redirect("/auth/login");
            }
        });
    }

    public static String encrypt(String text) {
        String hash = Hashing.sha256().hashString(text, StandardCharsets.UTF_8).toString();
        return hash;
    }

    public static void createUser(String user, String password) throws IOException {
        if (!isInDB(user)) {
            File file = new File(System.getProperty("user.dir") + STORE_FILE_PATH);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
            bw.write(user + " " + password);
            bw.flush();
            bw.close();
        }
    }

    public static boolean login(String user, String password) throws IOException {
        String path = System.getProperty("user.dir") + STORE_FILE_PATH;
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer stk = new StringTokenizer(line);
            if (user.equals(stk.nextToken()) && password.equals(stk.nextToken())) {
                return true;
            } else if (user.equals(stk.nextToken()) && !password.equals(stk.nextToken())) {
                return false;
            }
        }
        return false;
    }

    public static boolean isInDB(String user) throws IOException {
        String path = System.getProperty("user.dir") + STORE_FILE_PATH;
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer stk = new StringTokenizer(line);
            if (user.equals(stk.nextToken())) {
                return true;
            }
        }
        return false;
    }
}
