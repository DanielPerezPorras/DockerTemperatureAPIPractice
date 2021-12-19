import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Medicion {

	private static final DateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private final Date cuando;
	private final double valor;

	public Medicion(Date cuando, double valor) {
		if (cuando == null) {
			throw new IllegalArgumentException("'cuando' must not be null");
		}
		this.cuando = cuando;
		this.valor = valor;
	}

	public Medicion(String cuando, String valor) throws ParseException {
		this(FORMAT.parse(cuando), Double.parseDouble(valor));
	}

	public Date getCuando() {
		return cuando;
	}

	public String getCuandoString() {
		return FORMAT.format(cuando);
	}

	public double getValor() {
		return valor;
	}

}
