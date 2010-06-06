package domain.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.javainetlocator.InetAddressLocator;
import net.sf.javainetlocator.InetAddressLocatorException;
import pdclogs.LogsClient;
import tgp.TGPClient;
import tmp.TMPServer;
import wdp.WDPServer;
import domain.data.WorkerDAO;

public class WorkerServiceImpl implements WorkerService {

	private WorkerDAO workerDao;
	private TMPServer tmpServer = null;
	private TGPClient tgpClient = null;
	private LogsClient logsClient = null;
	private WDPServer wdpServer = null;
	private List<String> countries;
	private List<String> userAgents;
	private String datesParam;

	public WorkerServiceImpl(WorkerDAO workerDao) {
		this.workerDao = workerDao;
		tgpClient = new TGPClient("localhost", 8092, this);
		logsClient = new LogsClient("localhost", 8085, this);
		wdpServer = new WDPServer(this);
		tmpServer = new TMPServer(this);

	}

	private static final Pattern IPAdrresPattern = Pattern
			.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");

	private static final Pattern bytesTransferredPattern = Pattern.compile(
			"(?:POST|GET)[^\"]*\"\\s*\\d+\\s*(-|\\d+)",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern datePattern = Pattern
			.compile("(\\d{1,2})/(\\w{3})/(\\d{4})");

	private static final Map<String, Integer> dates;

	static {
		dates = new HashMap<String, Integer>();
		dates.put("Jan", 0);
		dates.put("Feb", 1);
		dates.put("Mar", 2);
		dates.put("Apr", 3);
		dates.put("May", 4);
		dates.put("Jun", 5);
		dates.put("Jul", 6);
		dates.put("Aug", 7);
		dates.put("Sep", 8);
		dates.put("Oct", 9);
		dates.put("Nov", 10);
		dates.put("Dic", 11);
	}

	@Override
	public void badRequest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void logError(String error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveLogs(String logs) {
		workerDao.setLogs(logs);
		processLogs();
	}

	@Override
	public void setGroup(int group) {
		workerDao.setGroup(group);
	}

	@Override
	public String getWorkerHost() {
		return workerDao.getWorkerHost();
	}

	@Override
	public int getWorkerPort() {
		return workerDao.getWorkerPort();
	}

	public TGPClient getTgpClient() {
		return tgpClient;
	}

	public LogsClient getLogsClient() {
		return logsClient;
	}

	@Override
	public String getResource() {
		return workerDao.getResource();
	}

	@Override
	public void setResource(String resource) {
		workerDao.setResource(resource);
	}

	@Override
	public void setParamsToProcess(List<String> countries,
			List<String> userAgents, String datesParam) {
		this.countries = countries;
		this.userAgents = userAgents;
		this.datesParam = datesParam;
	}

	@Override
	public void processLogs() {

		boolean hasUserAgents = (userAgents != null && !userAgents.isEmpty());
		boolean hasCountries = (countries != null && !countries.isEmpty());
		boolean hasDateRange = (datesParam != null);

		String logs[] = workerDao.getLogs().split("\n");

		Map<String, Integer> response = new HashMap<String, Integer>();

		response.put("hits", 0);
		response.put("bytes", 0);
		boolean countryAccepted = !hasCountries;
		boolean userAgentAccepted = !hasUserAgents;
		boolean datesAccepted = !hasDateRange;
		for (String log : logs) {
			if (hasUserAgents) {
				for (String userAgent : userAgents) {
					if (log.toLowerCase().contains(userAgent.toLowerCase().trim())) {
						System.out.println("log: "+log);
						userAgentAccepted = true;
					}
				}
			}
			if (hasCountries) {
				Matcher m = IPAdrresPattern.matcher(log);
				if (m.find()) {
					String ip = m.group(1);
					InetAddress ipAddress;
					try {
						ipAddress = InetAddress.getByAddress(ip, new byte[4]);
						Locale locale = InetAddressLocator.getLocale(ipAddress);
						String countryFromIp = locale.getDisplayCountry();
						for (String paramCountry : countries) {
							System.out.println("request from: "+locale);
							if (countryFromIp.equalsIgnoreCase(paramCountry)) {
								countryAccepted = true;
							}
						}
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					} catch (InetAddressLocatorException e) {
						e.printStackTrace();
					}
				}
			}
			if (hasDateRange) {
				Matcher m = datePattern.matcher(log);
				if (m.find()) {
					Calendar date = getDate(m.group(0));
					String aux[] = datesParam.split("-");
					if (aux.length == 2) {
						Calendar startDate = getDate(aux[0]);
						Calendar endDate = getDate(aux[1]);
						if (startDate.getTimeInMillis() <= date
								.getTimeInMillis()
								&& endDate.getTimeInMillis() >= date
										.getTimeInMillis()) {
							datesAccepted = true;
						}
					}
				}
			}
			if (datesAccepted && userAgentAccepted && countryAccepted) {
				Matcher m = bytesTransferredPattern.matcher(log);
				if (m.find()) {
					response.put("hits", response.get("hits") + 1);
					int bytesTransferred = 0;
					if (!m.group(1).equals("-")) {
						bytesTransferred = Integer.valueOf(m.group(1));
					}
					response.put("bytes", response.get("bytes")
							+ bytesTransferred);
				} else {
					System.out.println("no se encontraron bytes transferidos");
				}
			}

		}

		try {
			wdpServer.sendWorkDone(response);
		} catch (IOException e) {
			// TODO work could not be completed, inform.
			e.printStackTrace();
		}
	}

	private Calendar getDate(String date) {
		Matcher m = datePattern.matcher(date);
		if (m.find()) {
			Calendar gregorianDate = new GregorianCalendar(Integer.valueOf(m
					.group(3)), dates.get(m.group(2)), Integer.valueOf(m
					.group(1)));
			return gregorianDate;
		}
		return null;

	}

	@Override
	public WDPServer getWdpServer() {
		return this.wdpServer;
	}

	@Override
	public TMPServer getTmpServer() {
		return tmpServer;
	}

	@Override
	public void fetchResource(String resource, String hostname, int port) {
		try {
			logsClient.fetchResource(resource, hostname, port);
		} catch (IOException e) {
			// TODO: enviar mensaje de error a quien corresponda
			e.printStackTrace();
		}
	}

}
