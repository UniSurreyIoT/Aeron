package eu.neclab.iotplatform.ngsi.api.datamodel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class NgsiObjectFactory {

	/*
	 * ContextMetadata
	 */
	public static ContextMetadata generateContextMetadataSegment2d() {

		ContextMetadata contextMetadata = new ContextMetadata();
		contextMetadata.setName(MetadataTypes.SimpleGeolocation.getName());
		contextMetadata.setType(MetadataTypes.SimpleGeolocation.getType());
		contextMetadata.setValue(generateSegment2d());

		return contextMetadata;

	}

	public static ContextMetadata generateContextMetadataSegment3d() {

		ContextMetadata contextMetadata = new ContextMetadata();
		contextMetadata.setName(MetadataTypes.SimpleGeolocation.getName());
		contextMetadata.setType(MetadataTypes.SimpleGeolocation.getType());
		contextMetadata.setValue(generateSegment3d());
		return contextMetadata;

	}

	public static ContextMetadata generateContextMetadataUnitCelsius() {

		ContextMetadata contextMetadata = new ContextMetadata();
		contextMetadata.setName(MetadataTypes.Unit.getName());
		contextMetadata.setType(MetadataTypes.Unit.getType());
		contextMetadata.setValue("Celsius");
		return contextMetadata;

	}

	public static ContextMetadata generateContextMetadataUnitCar() {

		ContextMetadata contextMetadata = new ContextMetadata();
		contextMetadata.setName(MetadataTypes.Unit.getName());
		contextMetadata.setType(MetadataTypes.Unit.getType());
		contextMetadata.setValue("NumberOfCar");
		return contextMetadata;

	}

	public static ContextMetadata generateContextMetadataAccurancy() {

		ContextMetadata contextMetadata = new ContextMetadata();
		contextMetadata.setName("accurancy");
		try {
			contextMetadata.setType(new URI("accurancy"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		contextMetadata.setValue("1");
		return contextMetadata;

	}

	/*
	 * EntityIdList
	 */
	public static List<EntityId> generateEntityIdListFull() {
		List<EntityId> entityIdList = new ArrayList<EntityId>();
		entityIdList.add(generateEntityIdPatternWithType());
		entityIdList.add(generateEntityIdPatternWithoutType());
		entityIdList.add(generateEntityIdWithType());
		entityIdList.add(generateEntityIdWithoutType());

		return entityIdList;
	}

	/*
	 * EntityId
	 */

	public static EntityId generateEntityIdPatternWithType() {
		try {
			return new EntityId(".*", new URI("NGSI:Node"), true);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static EntityId generateEntityIdPatternWithoutType() {

		return new EntityId(".*", null, true);
	}

	public static EntityId generateEntityIdWithType() {
		try {
			return new EntityId("Bus34", new URI("NGSI:Node"), false);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static EntityId generateEntityIdWithoutType() {

		return new EntityId("Bismarckplatz", null, false);
	}

	/*
	 * OperationScopeList
	 */
	public static List<OperationScope> generateOperationScopeListFull() {
		List<OperationScope> operationScopeList = new ArrayList<OperationScope>();
		operationScopeList.add(generateOperationScopeSimpleGeolocation2d());
		operationScopeList.add(generateOperationScopeSimpleGeolocation3d());
		operationScopeList.add(generateISO8601TimeInterval());

		return operationScopeList;

	}

	/*
	 * Segment
	 */
	public static Segment generateSegment2d() {
		return new Segment("43.486160, -3.872040", "43.430821, -3.744806", null);
	}

	public static Segment generateSegment3d() {
		return new Segment("43.486160, -3.872040", "43.430821, -3.744806", 10.0);
	}

	/*
	 * Circle
	 */
	public static Circle generateCircle() {
		return new Circle(43.486160f, -3.872040f, 10f);
	}

	/*
	 * Point
	 */
	public static Point generatePoint() {
		return new Point(43.486160f, -3.872040f);
	}

	/*
	 * Polygon
	 */
	public static Polygon generatePolygon() {
		Polygon polygon = new Polygon();

		List<Vertex> vertexList = new ArrayList<Vertex>();
		vertexList.add(new Vertex(43.486160f, -3.872040f));
		vertexList.add(new Vertex(43.486165f, -3.872040f));
		vertexList.add(new Vertex(43.486165f, -3.872045f));
		vertexList.add(new Vertex(43.486160f, -3.872045f));
		vertexList.add(new Vertex(43.486160f, -3.872040f));
		polygon.setVertexList(vertexList);

		return polygon;
	}

	/*
	 * PEP Credentials
	 */
	public static PEPCredentials generatePEPCredentials() {
		return new PEPCredentials("user", "passwd");
	}

	/*
	 * OperationScope
	 */
	public static OperationScope generateOperationScopeSimpleGeolocation2d() {
		return new OperationScope(ScopeTypes.SimpleGeolocation,
				generateSegment2d());
	}

	public static OperationScope generateOperationScopeSimpleGeolocation3d() {
		return new OperationScope(ScopeTypes.SimpleGeolocation,
				generateSegment3d());
	}

	public static OperationScope generateISO8601TimeInterval() {
		return new OperationScope(ScopeTypes.ISO8601TimeInterval,
				"2015-06-13T17:07:16+0200/2015-06-13T17:07:18+0200");
	}

	/*
	 * Restriction
	 */
	public static Restriction generateRestrictionOnlyAttributeExpression() {
		return new Restriction(
				"//contextMetadata[name='metadataname'][value='valuevalue']",
				null);
	}

	public static Restriction generateRestrictionOnlyAttributeExpressionEmptyScopelist() {

		List<OperationScope> operationScope = new ArrayList<OperationScope>();

		return new Restriction(
				"//contextMetadata[name='metadataname'][value='valuevalue']",
				operationScope);
	}

	public static Restriction generateRestrictionFull() {

		return new Restriction(
				"//contextMetadata[name='metadataname'][value='valuevalue']",
				generateOperationScopeListFull());
	}

	/*
	 * AttributeList
	 */

	public static List<String> generateAttributeListFull() {
		List<String> attributeList = new ArrayList<String>();
		attributeList.add("NGSI:Noise");
		attributeList.add("NGSI:Occupancy");
		return attributeList;
	}

	public static List<String> generateAttributeListOneValue() {
		List<String> attributeList = new ArrayList<String>();
		attributeList.add("NGSI:Noise");
		return attributeList;
	}

	public static List<String> generateAttributeListEmpty() {
		List<String> attributeList = new ArrayList<String>();
		return attributeList;
	}

	public static List<ContextRegistration> generateContextRegistrationListOneValue() {

		List<ContextRegistration> contextRegistrationList = new ArrayList<ContextRegistration>();
		contextRegistrationList.add(generateContextRegistration());
		
		return contextRegistrationList;

	}

	public static URI generateProvidingApplicationURI() {
		URI uri = null;
		try {
			uri = new URI("http://localhost:8060/ngsi10");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;
	}

	public static ContextRegistration generateContextRegistration() {
		ContextRegistration contextRegistration = new ContextRegistration();

		contextRegistration
				.setListContextMetadata(new ArrayList<ContextMetadata>());

		contextRegistration.getListContextMetadata().add(
				generateContextMetadataSegment2d());

		contextRegistration.setListEntityId(generateEntityIdListFull());

		contextRegistration
				.setProvidingApplication(generateProvidingApplicationURI());

		contextRegistration
				.setListContextRegistrationAttribute(new ArrayList<ContextRegistrationAttribute>());
		contextRegistration.getContextRegistrationAttribute().add(
				generateContextRegistrationAttributeCarCount());
		contextRegistration.getContextRegistrationAttribute().add(
				generateContextRegistrationAttributeTemperature());

		return contextRegistration;

	}

	public static ContextRegistrationAttribute generateContextRegistrationAttributeTemperature() {
		ContextRegistrationAttribute contextRegistrationAttribute = null;
		try {
			contextRegistrationAttribute = new ContextRegistrationAttribute(
					"m3-lite:AirTemperature",
					new URI("m3-lite:AirTemperature"), false,
					new ArrayList<ContextMetadata>());

			contextRegistrationAttribute.getMetadata().add(
					generateContextMetadataUnitCelsius());

			contextRegistrationAttribute.getMetadata().add(
					generateContextMetadataAccurancy());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return contextRegistrationAttribute;
	}

	public static ContextRegistrationAttribute generateContextRegistrationAttributeCarCount() {
		ContextRegistrationAttribute contextRegistrationAttribute = null;
		try {
			contextRegistrationAttribute = new ContextRegistrationAttribute(
					"m3-lite:CarCount", new URI("m3-lite:CarCount"), false,
					new ArrayList<ContextMetadata>());

			contextRegistrationAttribute.getMetadata().add(
					generateContextMetadataUnitCar());

			contextRegistrationAttribute.getMetadata().add(
					generateContextMetadataAccurancy());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return contextRegistrationAttribute;
	}
}
