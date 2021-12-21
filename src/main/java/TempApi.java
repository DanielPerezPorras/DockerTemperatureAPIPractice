import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static spark.Spark.*;
import spark.Request;
import redis.clients.jedis.Jedis;

public class TempApi {

	private static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST","localhost");
	private static Jedis jedis;
	
	public static void main(String[] args) {
		try {

			System.out.println("Conectando con " + REDIS_HOST);
			jedis = new Jedis(REDIS_HOST);

			// Borra el contenido previo
			// jedis.flushAll();

			jedis.close();

			// Endpoints
			get("/nuevo/:dato", TempApi::nuevo);
			get("/listar", TempApi::listar);
			get("/grafica", TempApi::grafica);
			get("/listajson", TempApi::listajson);

		} catch (Exception ex) {
			System.err.println("No ha sido posible conectar con " + REDIS_HOST);
		}
	}


	// -- Endpoints -----------------------------------------------------------------------------------------------


	private static String nuevo(Request req, spark.Response res) {
		try {
			addMedicion(Double.parseDouble(req.params(":dato")));
			return "Medida registrada";
		} catch (NumberFormatException ex) {
			return "Medida no válida";
		}
	}

	private static String listar(Request req, spark.Response res) {
		try {
			StringBuilder builder = new StringBuilder();

			builder.append("<h1>Mediciones tomadas</h1>");
			builder.append("<table>");
			builder.append("<thead><tr><th>Fecha y hora</th><th>Valor</th></tr></thead>");
			builder.append("<tbody>");

			List<Medicion> mediciones = getAllMediciones();
			for (Medicion m : mediciones) {
				builder.append("<tr><td>")
						.append(m.getCuandoString())
						.append("</td><td style=\"text-align:right\">")
						.append(m.getValor())
						.append("</td></tr>");
			}

			builder.append("</tbody>");
			builder.append("</table>");

			return builder.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Fallo en el manejador";
		}

	}

	private static String grafica(Request req, spark.Response res) {
		return createChartWebPage(System.getenv("HOSTNAME"));
	}

	private static String listajson(Request req, spark.Response res) {
		res.type("application/json");
		return getUltimasMedicionesJSON();
	}


	// ------------------------------------------------------------------------------------------------------------


	private static List<Medicion> getAllMediciones() {
		jedis = new Jedis(REDIS_HOST);
		List<Medicion> mediciones = new ArrayList<>();

		long numMediciones = jedis.llen("queue#fechas");
		for (long i = 0; i < numMediciones; i++) {
			String fecha = jedis.lindex("queue#fechas", i);
			String dato = jedis.lindex("queue#datos", i);
			try {
				Medicion med = new Medicion(fecha, dato);
				mediciones.add(med);
			} catch (ParseException | NumberFormatException ignored) { }
		}

		jedis.close();
		return mediciones;
	}

	@SuppressWarnings("unchecked")
	private static String getUltimasMedicionesJSON() {
		JSONObject resultado = new JSONObject();
		JSONArray medicionesArray = new JSONArray();
		List<Medicion> mediciones = getAllMediciones();
		int tam = mediciones.size();
		int desde = tam > 10 ? tam - 10 : 0;
		for (int i = desde; i < tam; i++) {
			Medicion m = mediciones.get(i);
			JSONObject medJson = new JSONObject();
			medJson.put("time", m.getCuandoString());
			medJson.put("valor", m.getValor());
			medicionesArray.add(medJson);
		}
		resultado.put("Mediciones", medicionesArray);
		return resultado.toJSONString();
	}

	private static void addMedicion(double valor) {
		jedis = new Jedis(REDIS_HOST);

		Medicion med = new Medicion(new Date(), valor);

		jedis.rpush("queue#fechas", med.getCuandoString());
		jedis.rpush("queue#datos", med.getValor() + "");

		jedis.close();
	}

	private static String createChartWebPage(String hostname) {
		return  "<!DOCTYPE html>" +
				"<html lang='en'>" +
				"<head>" +
				"  <meta charset='UTF-8'>" +
				"  <meta name='author' content='Daniel Pérez Porras'>" +
				"  <title>Gráfica</title>" +
				"  <script src='https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.0/Chart.js'></script>" +
				"</head>" +
				"<body>" +
				"  <canvas id='c' style='border:1px solid #555;'></canvas>" +
				"  <script>" +
				"  var ctxt = document.getElementById('c').getContext('2d');" +
				"  var myLineChart = new Chart(ctxt, {" +
				"    type: 'line'," +
				"    data: {" +
				"      datasets: [" +
				"        {label : '" + hostname + "'}" +
				"      ]," +
				"      labels: []" +
				"    }" +
				"  });" +
				"  myLineChart.options.animation = false;" +
				"  actualizarGrafica = function() {" +
				"    var xhttp = new XMLHttpRequest();" +
				"    xhttp.onreadystatechange = function() {" +
				"      if (this.readyState == 4 && this.status == 200) {" +
				"        obj = JSON.parse(xhttp.responseText);" +
				"        for (i=0;i<obj.Mediciones.length;i++) {" +
				"          myLineChart.data.datasets[0].data[i] = obj.Mediciones[i].valor;" +
				"          var time = obj.Mediciones[i].time.replace(/(\\d+)\\/(\\d+)\\/(\\d+) /, '$3-$2-$1T');" +
				"          myLineChart.data.labels[i] = new Date(time).toLocaleString();" +
				"        }" +
				"      }" +
				"    };" +
				"    xhttp.open(\"GET\", \"listajson\", true);" +
				"    xhttp.send();" +
				"	 myLineChart.update();" +
				"  };" +
				"  actualizarGrafica();" +
				"  setInterval(actualizarGrafica, 2000);" +
				"  </script>" +
				"</body>" +
				"</html>";
	}

}
