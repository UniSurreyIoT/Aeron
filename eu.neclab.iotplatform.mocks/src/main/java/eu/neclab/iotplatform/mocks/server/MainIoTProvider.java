package eu.neclab.iotplatform.mocks.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.neclab.iotplatform.iotbroker.commons.ContentType;
import eu.neclab.iotplatform.iotbroker.commons.FullHttpRequester;
import eu.neclab.iotplatform.mocks.utils.Mode;
import eu.neclab.iotplatform.mocks.utils.NGSIRequester;
import eu.neclab.iotplatform.mocks.utils.RangesUtil;
import eu.neclab.iotplatform.mocks.utils.ServerConfiguration;
import eu.neclab.iotplatform.ngsi.api.datamodel.ContextRegistration;
import eu.neclab.iotplatform.ngsi.api.datamodel.ContextRegistrationAttribute;
import eu.neclab.iotplatform.ngsi.api.datamodel.EntityId;
import eu.neclab.iotplatform.ngsi.api.datamodel.RegisterContextRequest;

public class MainIoTProvider {

	private static Logger logger = Logger.getLogger(MainIoTProvider.class);

	// Port Numbers
	private static String portNumbers;

	// Mode of the IoT Provider
	private static Mode mode;

	// Ranges of allowed EntityIds
	private static String rangesOfEntityIds;

	// Number of EntityIds to select amongst the EntityIds.
	private static int numberOfEntityIdsToSelect;

	// Ranges of allowed Attributes
	private static String rangesOfAttributes;

	// Number of Attributes to select amongst the Attributes.
	private static int numberOfAttributesToSelect;

	// Get the IoT Discovery URL
	private static String exposedURL;

	// Get the IoT Discovery URL
	private static ContentType outgoingContentType;

	private static boolean doRegistration;

	private static String queryContextResponseFile;

	private static String registerContextAvailabilityFile;

	// Configurations file
	private static String configurationFile = System
			.getProperty("eu.neclab.ioplatform.mocks.iotprovider.configurationFile");

	// Configurations map
	private static Map<String, String> configurations;

	// Get the IoT Discovery URL
	private static String iotDiscoveryURL = System.getProperty(
			"eu.neclab.ioplatform.mocks.iotDiscoveryUrl",
			"http://localhost:8065/");

	private static Set<Integer> portSet;

	public static void main(String[] args) {

		if (configurationFile != null) {
			readConfigurations(configurationFile);
		} else {
			logger.warn("Impossible to read configuration file: "
					+ configurationFile);
		}

		setBasicConfigurations();

		// Set<Integer> portSet = RangesUtil.rangesToSet(portNumbers);
		//
		// if (portSet == null) {
		// System.out
		// .println("Wrong eu.neclab.ioplatform.mocks.iotconsumer.ports property. "
		// +
		// "Allowed only ranges (e.g. 8001-8005) and single ports (e.g. 8001) separated by comma. "
		// +
		// "E.g. -Deu.neclab.ioplatform.mocks.iotconsumer.ports=8001-8005,8021,8025,8030-8040"
		// + " or similarly in the configuation file ");
		// System.exit(0);
		// }

		NGSIRequester ngsiRequester = new NGSIRequester();

		for (int portNumber : portSet) {
			
			/*
			 * Specific configuration for a specific server
			 */
			Mode serverMode;
			String serverQueryContextResponseFile;
			if (configurations != null) {
				serverMode = (Mode.fromString(configurations
						.get("eu.neclab.ioplatform.mocks.iotprovider."
								+ portNumber + ".mode"), mode));

				serverQueryContextResponseFile = configurations.getOrDefault(
						"eu.neclab.ioplatform.mocks.iotprovider." + portNumber
								+ ".queryContextResponseFile",
						queryContextResponseFile);
			} else {
				serverMode = mode;
				serverQueryContextResponseFile = queryContextResponseFile;
			}

			ServerDummy server = new ServerDummy();

			ServerConfiguration serverConfigurations = new ServerConfiguration();
			serverConfigurations.setPort(portNumber);
			serverConfigurations.setMode(serverMode);
			serverConfigurations
					.setQueryContextResponseFile(serverQueryContextResponseFile);

			try {

				server.startServer(portNumber,
						"eu.neclab.iotplatform.mocks.iotprovider",
						serverConfigurations);

			} catch (BindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
			}

			if (doRegistration) {
				if (serverMode == Mode.RANDOM) {

					Set<String> entityNames = chooseEntityNames();
					Set<String> attributes = chooseAttributes();

					RegisterContextRequest registration = createRegisterContextRequest(
							entityNames, attributes, portNumber);

					// TODO use the SouthBound class
					ngsiRequester.doRegistration(registration, ContentType.XML);

				} else {
					if (registerContextAvailabilityFile != null) {
						String registration = readRegisterContextAvailabilityFile(registerContextAvailabilityFile);
						if (registration != null) {
							try {
								FullHttpRequester.sendPost(new URL(
										iotDiscoveryURL), registration,
										ContentType.XML.toString());
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {
						try {
							FullHttpRequester.sendPost(new URL(iotDiscoveryURL
									+ "/ngsi9/registerContext"),
									getDefaultRegistration(portNumber),
									ContentType.XML.toString());
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}

		}

	}

	private static String readRegisterContextAvailabilityFile(String file) {

		String response = null;

		try {
			response = new Scanner(new File(file)).useDelimiter("\\Z").next();

			if (logger.isDebugEnabled()) {
				logger.debug("Registration read from file: " + response);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;

	}

	private static void readConfigurations(String file) {

		configurations = new HashMap<String, String>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.matches("#.*")) {
					continue;
				}
				if (line.matches(".*=.*")) {
					String[] keyValue = line.split("=");
					configurations.put(keyValue[0],
							keyValue[1].replace("\"", ""));
				} else {
					logger.warn("Wrong property in the configuration file: "
							+ line);
				}
			}
		} catch (FileNotFoundException e) {
			logger.warn("FileNotFoundException: Impossible to read configuration file: "
					+ configurationFile);
		} catch (IOException e) {
			logger.warn("IOException: Impossible to read configuration file: "
					+ configurationFile);
		}

	}

	private static void setBasicConfigurations() {

		/*
		 * Port Numbers
		 */
		if (configurations != null) {
			portNumbers = configurations.getOrDefault(
					"eu.neclab.ioplatform.mocks.iotprovider.ports",
					ServerConfiguration.DEFAULT_PORTNUMBERS);
		} else {
			portNumbers = ServerConfiguration.DEFAULT_PORTNUMBERS;
		}
		portSet = RangesUtil.rangesToSet(portNumbers);

		/*
		 * defaultMode
		 */
		if (configurations != null) {
			mode = Mode.fromString(configurations
					.get("eu.neclab.ioplatform.mocks.iotprovider.mode"),
					ServerConfiguration.DEFAULT_MODE);
		} else {
			mode = ServerConfiguration.DEFAULT_MODE;
		}

		/*
		 * Ranges of allowed EntityIds
		 */
		if (configurations != null) {
			rangesOfEntityIds = configurations.getOrDefault(
					"eu.neclab.ioplatform.mocks.iotprovider.rangesOfEntityIds",
					ServerConfiguration.DEFAULT_RANGESOFENTITYIDS);
		} else {
			rangesOfEntityIds = ServerConfiguration.DEFAULT_RANGESOFENTITYIDS;
		}

		/*
		 * Number of EntityIds to select amongst the EntityIds.
		 */
		if (configurations != null) {
			numberOfEntityIdsToSelect = Integer
					.parseInt(configurations
							.getOrDefault(
									"eu.neclab.ioplatform.mocks.iotprovider.numberOfEntityIdsToSelect",
									ServerConfiguration.DEFAULT_RANGESOFENTITYIDSTOSELECT));
		} else {
			numberOfEntityIdsToSelect = Integer
					.parseInt(ServerConfiguration.DEFAULT_RANGESOFENTITYIDSTOSELECT);
		}

		/*
		 * Ranges of allowed Attributes
		 */
		if (configurations != null) {
			rangesOfAttributes = configurations
					.getOrDefault(
							"eu.neclab.ioplatform.mocks.iotprovider.rangesOfAttributes",
							ServerConfiguration.DEFAULT_RANGESOFATTRIBUTES);
		} else {
			rangesOfAttributes = ServerConfiguration.DEFAULT_RANGESOFATTRIBUTES;
		}

		/*
		 * Number of Attributes to select amongst the Attributes.
		 */
		if (configurations != null) {
			numberOfAttributesToSelect = Integer
					.parseInt(configurations
							.getOrDefault(
									"eu.neclab.ioplatform.mocks.iotprovider.numberOfAttributesToSelect",
									ServerConfiguration.DEFAULT_RANGESOFATTRIBUTESTOSELECT));
		} else {
			numberOfAttributesToSelect = Integer
					.parseInt(ServerConfiguration.DEFAULT_RANGESOFATTRIBUTESTOSELECT);
		}

		/*
		 * ExposedUrl
		 */
		if (configurations != null) {
			exposedURL = configurations.getOrDefault(
					"eu.neclab.ioplatform.mocks.iotprovider.exposedURL",
					ServerConfiguration.DEFAULT_EXPOSEDURL);
		} else {
			exposedURL = ServerConfiguration.DEFAULT_EXPOSEDURL;
		}

		/*
		 * doRegistration
		 */
		if (configurations != null) {
			doRegistration = Boolean.parseBoolean(configurations.getOrDefault(
					"eu.neclab.ioplatform.mocks.iotprovider.doRegistration",
					ServerConfiguration.DEFAULT_DOREGISTRATION));
		} else {
			doRegistration = Boolean
					.parseBoolean(ServerConfiguration.DEFAULT_DOREGISTRATION);
		}

		/*
		 * queryContextResponseFile
		 */
		if (configurations != null) {
			queryContextResponseFile = configurations
					.getOrDefault(
							"eu.neclab.ioplatform.mocks.iotprovider.queryContextResponseFile",
							ServerConfiguration.DEFAULT_QUERYCONTEXTRESPONSEFILE);
		} else {
			queryContextResponseFile = ServerConfiguration.DEFAULT_QUERYCONTEXTRESPONSEFILE;
		}

		/*
		 * queryContextResponseFile
		 */
		if (configurations != null) {
			registerContextAvailabilityFile = configurations
					.get("eu.neclab.ioplatform.mocks.iotprovider.registerContextAvailabilityFile");
		}

	}

	private static Set<String> chooseEntityNames() {

		// This Set will contain the chosen entityIds Set
		Set<String> entityIdSet = new HashSet<String>();

		Object[] entityIdsAvailable = RangesUtil.rangesToSet(rangesOfEntityIds)
				.toArray();

		if (numberOfEntityIdsToSelect >= entityIdsAvailable.length) {
			System.out
					.println("WARN: numberOfEntityIdsToSelect >= entityIdsAvailable.length");
			for (Object id : entityIdsAvailable) {
				entityIdSet.add("EntityId-" + id);
			}
		} else {
			Random rand = new Random();
			int count = 0;
			while (count < numberOfEntityIdsToSelect) {
				if (entityIdSet.add("EntityId-"
						+ entityIdsAvailable[rand
								.nextInt(entityIdsAvailable.length)])) {
					count++;
				}
			}
		}

		return entityIdSet;

	}

	private static Set<String> chooseAttributes() {

		// This Set will contain the chosen attributes Set
		Set<String> attributeSet = new HashSet<String>();

		Object[] attributesAvailable = RangesUtil.rangesToSet(
				rangesOfAttributes).toArray();

		if (numberOfAttributesToSelect >= attributesAvailable.length) {
			System.out
					.println("WARN: numberOfAttributesToSelect >= attributesAvailable.length");
			for (Object attribute : attributesAvailable) {
				attributeSet.add("Attribute-" + attribute);
			}
		} else {
			Random rand = new Random();
			int count = 0;
			while (count < numberOfAttributesToSelect) {
				if (attributeSet.add("Attribute-"
						+ attributesAvailable[rand
								.nextInt(attributesAvailable.length)])) {
					count++;
				}
			}

		}

		return attributeSet;

	}

	private static RegisterContextRequest createRegisterContextRequest(
			Set<String> entityNames, Set<String> attributes, int port) {
		// Create the entityIdList to put into the registration
		List<EntityId> entityIdList = new ArrayList<EntityId>();
		for (String entityName : entityNames) {
			entityIdList.add(new EntityId(entityName, null, false));
		}

		// Create the contextRegistrationAttribute list to put into the
		// registration
		List<ContextRegistrationAttribute> contextRegistrationAttributes = new ArrayList<ContextRegistrationAttribute>();
		for (String attribute : attributes) {
			contextRegistrationAttributes.add(new ContextRegistrationAttribute(
					attribute, null, false, null));
		}

		// Create the context Registration
		ContextRegistration contextRegistration = new ContextRegistration();
		contextRegistration.setListEntityId(entityIdList);
		contextRegistration
				.setListContextRegistrationAttribute(contextRegistrationAttributes);

		// Form the providing application
		URI providingApplication = null;
		try {
			String ref;
			if (exposedURL != null) {
				ref = exposedURL + ":" + port + "/ngsi10/";
				;
			} else {
				ref = "http://" + InetAddress.getLocalHost().getHostAddress()
						+ ":" + port + "/ngsi10/";
			}

			providingApplication = new URI(ref);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		contextRegistration.setProvidingApplication(providingApplication);

		// Create the contextRegistration list
		List<ContextRegistration> contextRegistrationList = new ArrayList<ContextRegistration>();
		contextRegistrationList.add(contextRegistration);

		// Create the registration
		RegisterContextRequest registration = new RegisterContextRequest();
		registration.setContextRegistrationList(contextRegistrationList);

		return registration;
	}

	private static String getDefaultRegistration(int port) {
		String registration = ServerConfiguration.DEFAULT_REGISTERCONTEXTAVAILABILITY;
		try {
			String ref;
			if (exposedURL != null) {
				ref = exposedURL + ":" + port + "/ngsi10/";
				;
			} else {
				ref = "http://" + InetAddress.getLocalHost().getHostAddress()
						+ ":" + port + "/ngsi10/";
			}

			registration = registration.replace(
					"PROVIDINGAPPLICATION_PLACEHOLDER", ref);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return registration;
	}

	// private static void doRegistration(RegisterContextRequest registration) {
	//
	// // Get the IoT Discovery URL
	// String iotDiscoveryURL = System.getProperty(
	// "eu.neclab.ioplatform.mocks.iotprovider.iotDiscoveryUrl",
	// "http://localhost:8065/");
	//
	// RegisterContextResponse output = new RegisterContextResponse();
	//
	// try {
	//
	// Object response = sendRequest(new URL(iotDiscoveryURL), "/"
	// + "ngsi9" + "/" + "registerContext", registration,
	// RegisterContextResponse.class);
	//
	// // If there was an error then a StatusCode has been returned
	// if (response instanceof StatusCode) {
	// output = new RegisterContextResponse(null, null,
	// (StatusCode) response);
	// }
	//
	// // Cast the response
	// output = (RegisterContextResponse) response;
	//
	// } catch (MalformedURLException e) {
	// logger.warn("Malformed URI", e);
	//
	// output = new RegisterContextResponse(null, null, new StatusCode(
	// Code.INTERNALERROR_500.getCode(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString()));
	//
	// } catch (IOException e) {
	// logger.warn("I/O Exception", e);
	//
	// output = new RegisterContextResponse(null, null, new StatusCode(
	// Code.INTERNALERROR_500.getCode(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString()));
	//
	// } catch (URISyntaxException e) {
	// logger.warn("URISyntaxException", e);
	// }
	// }
	//
	// /**
	// * Calls the QueryContext method on an NGSI-10 server.
	// *
	// * @return A StatusCode if there was an error, otherwise an object of the
	// * expectedResponseClazz
	// *
	// */
	// private static Object sendRequest(URL url, String resource,
	// NgsiStructure request, Class<?> expectedResponseClazz) {
	//
	// ContentType preferredContentType = getCONTENT_TYPE();
	//
	// Object output;
	//
	// try {
	// String correctedResource;
	// if (url.toString().isEmpty() || url.toString().matches(".*/")) {
	// correctedResource = resource;
	// } else {
	// correctedResource = "/" + resource;
	// }
	//
	// FullHttpResponse response = sendPostTryingAllSupportedContentType(
	// new URL(url + correctedResource), request,
	// preferredContentType, correctedResource);
	//
	// if (response.getStatusLine().getStatusCode() == 415) {
	//
	// logger.warn("Content Type is not supported by the receiver! URL: "
	// + url + correctedResource);
	//
	// // TODO make a better usage of the Status Code
	// output = new StatusCode(
	// Code.INTERNALERROR_500.getCode(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString(),
	// "Content Type is not supported by the receiver! (application/xml and application/json tried)");
	// return output;
	//
	// }
	//
	// if (response.getStatusLine().getStatusCode() == 500) {
	//
	// logger.warn("Receiver Internal Error. URL: " + url
	// + correctedResource + ". "
	// + response.getStatusLine().getReasonPhrase());
	//
	// // TODO make a better usage of the Status Code
	// output = new StatusCode(Code.INTERNALERROR_500.getCode(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString(),
	// "Final receiver internal error: "
	// + response.getStatusLine().getReasonPhrase());
	// return output;
	//
	// }
	//
	// if (response.getStatusLine().getStatusCode() == 503) {
	//
	// logger.warn("Service Unavailable. URL: " + url
	// + correctedResource + ". "
	// + response.getStatusLine().getReasonPhrase());
	//
	// // TODO make a better usage of the Status Code
	// output = new StatusCode(Code.INTERNALERROR_500.getCode(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString(),
	// "Receiver service unavailable: "
	// + response.getStatusLine().getReasonPhrase());
	// return output;
	//
	// }
	//
	// // Check if there is a body
	// if (response.getBody() == null || response.getBody().isEmpty()) {
	//
	// logger.warn("Response from remote server empty");
	//
	// // TODO make a better usage of the Status Code
	// output = new StatusCode(Code.INTERNALERROR_500.getCode(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString(),
	// "Receiver response empty");
	//
	// return output;
	//
	// }
	//
	// // Get the ContentType of the response
	// ContentType responseContentType = getContentTypeFromResponse(
	// response, preferredContentType);
	//
	// // Check if the message is valid
	// if (response.getBody() != null
	// && !validateMessageBody(response.getBody(),
	// responseContentType.toString(),
	// expectedResponseClazz, ngsi10schema)) {
	//
	// logger.warn("Response from remote server non a valid NGSI message");
	//
	// // TODO make a better usage of the Status Code
	// output = new StatusCode(Code.INTERNALERROR_500.getCode(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString(),
	// "Receiver response non a valid NGSI message");
	//
	// return output;
	//
	// }
	//
	// // Finally parse it
	// output = parseResponse(response.getBody(), responseContentType,
	// expectedResponseClazz);
	//
	// } catch (MalformedURLException e) {
	// logger.warn("Malformed URI", e);
	//
	// // TODO make a better usage of the Status Code
	// output = new StatusCode(Code.INTERNALERROR_500.getCode(),
	// ReasonPhrase.RECEIVERINTERNALERROR_500.toString(), null);
	//
	// }
	//
	// return output;
	//
	// }
}