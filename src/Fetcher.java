import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import de.mbenning.weather.wunderground.api.domain.DataSet;
import de.mbenning.weather.wunderground.api.domain.WeatherStation;
import de.mbenning.weather.wunderground.impl.services.HttpDataReaderService;


public class Fetcher {
	private static int[] times = {2,8,14,20};
	private static HttpDataReaderService dataReader;
	private static java.sql.Connection conn;
	private static SimpleDateFormat sqlDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if ((args.length == 1) && (args[0].equals("-h") )) {
			usage();
			System.exit(0);
		}
		if (args.length < 3) {
			System.out.println("Invalid parameters:");
			usage();
			System.exit(1);
		}
		if ((!args[0].equals("-h")) &&
			(!args[0].equals("-s")) &&
			(!args[0].equals("-r")) ) {
			System.out.println("Invalid parameters:");
			usage();
			System.exit(1);
		}

		Calendar startDt = Calendar.getInstance();
		Calendar endDt = Calendar.getInstance();
		String[] stations = new String[args.length];

		String[] ymd = args[1].split(",");
		int y = Integer.parseInt(ymd[0]);
		int m = Integer.parseInt(ymd[1])-1;
		int d = Integer.parseInt(ymd[2]);
		startDt.set(y, m, d);

		conn = linktodata();
		
		if (args[0].equals("-s")) {
			endDt.set(y, m, d);
			stations = Arrays.copyOfRange(args, 2, args.length);
		}
		if (args[0].equals("-r")) {
			ymd = args[2].split(",");
			y = Integer.parseInt(ymd[0]);
			m = Integer.parseInt(ymd[1])-1;
			d = Integer.parseInt(ymd[2]);
			endDt.set(y, m, d);
			stations = Arrays.copyOfRange(args, 3, args.length);
		}
		if (stations[0].equalsIgnoreCase("auto")) {
			stations = getStations();
		}
		
		while (!startDt.after(endDt)) {
			System.out.println("Start=" + sqlDate.format(startDt.getTime()) + "   End=" + sqlDate.format(endDt.getTime()));
			for (String station : stations) {
				dataReader = new HttpDataReaderService();
				WeatherStation weatherStation = new WeatherStation(station);
				dataReader.setWeatherStation(weatherStation);
				dataReader.setWeatherDate(startDt.getTime());
				for (int time : times) {
					getRecord(time);
				}
			}
			startDt.add(Calendar.DAY_OF_MONTH, 1);
		}
		System.exit(0);
		
	}
	
	private static void usage() {
		System.out.println("Fetcher -h");
		System.out.println("        Displays this usage");
		System.out.println(" ");
		System.out.println("Fetcher -s yyyy,mm,dd <station>");
		System.out.println("        Obtains 4 readings for the given date and station");
		System.out.println("        Output is displayed and stored in the weather DB");
		System.out.println(" ");
		System.out.println("Fetcher -r yyyy,mm,dd yyyy,mm,dd <station>");
		System.out.println("        Obtains 4 readings for each date between the first and second dates");
		System.out.println("        Output is displayed and stored in the weather DB");
		System.out.println(" ");
	}
	private static void getRecord(int hour) {
		System.out.println(dataReader.getWeatherStation().getStationId());
		DataSet current = dataReader.getFirstDataSetByHour(hour);
		if (current != null) {
			System.out.println(current.getDateTime());
			System.out.println("Temperature: " + current.getTemperature());
			System.out.println("Humidity: " + current.getHumidity());
			System.out.println("Dew Point: " + current.getDewPoint());
			System.out.println("Wind Dir.: " + current.getWindDirection());
			System.out.println("Pressure Pa: " + current.getPressurehPa());
			System.out.println("Wind Dir.: " + current.getWindDirectionDegrees());
			System.out.println("Wind Speed Mph: " + current.getWindSpeedMph());
			System.out.println("Wind Gust Mph: " + current.getWindGustMph());
			insertLog(current);
		} else {
			System.out.println("No matching record found");
		}
	}
	private static java.sql.Connection linktodata () {
		
		String server = System.getenv("WeatherGrabber_DB_Server");
		String database = System.getenv("WeatherGrabber_DB_Database");
		String user = System.getenv("WeatherGrabber_DB_User");
		String password = System.getenv("WeatherGrabber_DB_Password");
		
		String connStr = "jdbc:mysql://" +
				         server +
				         "/" +
				         database +
				         "?user=" +
				         user +
				         "&password=" +
				         password;
		
        java.sql.Connection conn = null;
        
        try {
                Class.forName("org.gjt.mm.mysql.Driver").newInstance();
                }
        catch (Exception e) { }
        try {
                conn = java.sql.DriverManager.getConnection(connStr);
        }
        catch (Exception e) { }
        return conn;
	}
	public static String insertLog(DataSet current) {
        String todo = ("INSERT into Log " +
                       "(`Station`,`When`,`Temperature`,`Humidity`,`DewPoint`," +
                       " `WindDir`,`WindDirDeg`,`WindSpeed`,`WindGust`,`Pressure`,`RainRate`) "+
                       "values ('" + current.getWeatherStation().getStationId() +
                       "', '" + sqlDate.format(current.getDateTime()) +
                       "', " + current.getTemperature() +
                       ", " + current.getHumidity() +
                       ", " + current.getDewPoint() +
                       ", '" + current.getWindDirection() +
                       "', " + current.getWindDirectionDegrees() +
                       ", " + (current.getWindSpeedMph() > 0 ? current.getWindSpeedMph() : 0) +
                       ", " + (current.getWindGustMph() > 0 ? current.getWindGustMph() : 0) +
                       ", " + (current.getPressurehPa() > 0 ? current.getPressurehPa() : 0) +
                       ", " + (current.getRainRateHourlyIn() > 0 ? current.getRainRateHourlyIn() : 0) +
                       ")") ;
        try {
                java.sql.Statement s = conn.createStatement();
                s.executeUpdate (todo);
        }
        catch (Exception e) {
                return ("Oh oops - code 003\n"+e);
                }

        return (todo);

    }
	public static String[] getStations() {
        String cntQry = "SELECT count(*) FROM Xref";
        try {
        	java.sql.Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery(cntQry);
            rs.next();
            String[] stations = new String[rs.getInt(1)];
            rs.close();
            s.close();
            int a = 0;
            String todo = "SELECT `Station` FROM Xref WHERE Active = true ORDER BY 1";
            try {
            	s = conn.createStatement();
                rs = s.executeQuery(todo);
                while (rs.next()) {
                	stations[a++] = rs.getString(1);
                }
            }
            catch (Exception e) {
            	return (new String[] {"Oh oops - code 003\n"+e});
            }
            return (stations);
        }
        catch (Exception e) {
        	return (new String[] {"Oh oops - code 003\n"+e});
        }
    }
}
