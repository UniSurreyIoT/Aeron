/*******************************************************************************
 *   Copyright (c) 2014, NEC Europe Ltd.
 *   All rights reserved.
 *
 *   Authors:
 *           * Salvatore Longo - salvatore.longo@neclab.eu
 *           * Tobias Jacobs - tobias.jacobs@neclab.eu
 *           * Raihan Ul-Islam - raihan.ul-islam@neclab.eu
 *
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   3. All advertising materials mentioning features or use of this software
 *     must display the following acknowledgement:
 *     This product includes software developed by NEC Europe Ltd.
 *   4. Neither the name of the NEC nor the
 *     names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY NEC ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL NEC BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package eu.neclab.iotplatform.iotbroker.core.junittests.tests;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import eu.neclab.iotplatform.iotbroker.commons.XmlFactory;
import eu.neclab.iotplatform.iotbroker.commons.interfaces.ResultFilterInterface;
import eu.neclab.iotplatform.iotbroker.core.IotBrokerCore;
import eu.neclab.iotplatform.ngsi.api.datamodel.DiscoverContextAvailabilityRequest;
import eu.neclab.iotplatform.ngsi.api.datamodel.QueryContextRequest;
import eu.neclab.iotplatform.ngsi.api.datamodel.QueryContextResponse;
import eu.neclab.iotplatform.ngsi.api.datamodel.UpdateContextRequest;
import eu.neclab.iotplatform.ngsi.api.datamodel.UpdateContextResponse;
import eu.neclab.iotplatform.ngsi.api.ngsi10.Ngsi10Requester;
import eu.neclab.iotplatform.ngsi.api.ngsi9.Ngsi9Interface;

public class TestFunctionality {

	SupportingFunctions util = new SupportingFunctions();
	XmlFactory xmlFactory = new XmlFactory();
	IotBrokerCore iotBrokerCore;
	Ngsi9Interface ngsi9Interface;
	Ngsi10Requester ngsi10Requestor;
	ResultFilterInterface resultFilterInterface;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ngsi9Interface = EasyMock.createStrictMock(Ngsi9Interface.class);
		ngsi10Requestor = EasyMock.createStrictMock(Ngsi10Requester.class);
		resultFilterInterface= EasyMock.createMock(ResultFilterInterface.class);
		iotBrokerCore = new IotBrokerCore();
		iotBrokerCore.setNgsi9Impl(ngsi9Interface);
		iotBrokerCore.setNgsi10Requestor(ngsi10Requestor);
		ReflectionTestUtils.setField(iotBrokerCore, "pubSubUrl", "http://127.0.0.1");

	}

	@After
	public void tearDown() throws Exception {
	}


	/*
	 * Basic query context request junit test
	 */
	@Test
	public void testQueryContext() {

		QueryContextRequest queryContextRequest = util.prepareQueryContextRequest("/queryContextRequest.xml");
		QueryContextResponse queryContextResponse = util.prepareQueryContextResponse("/queryContextResponse.xml");



		DiscoverContextAvailabilityRequest dca= util.prepareDiscoverContextAvailabilityRequest("/discoverContextAvailabilityRequest.xml");
		EasyMock.expect(
						ngsi9Interface
								.discoverContextAvailability(dca))
						.andReturn(
								util.prepareDiscoverContextAvailabilityResponse("/discoverContextAvailabilityResponse.xml"));
		EasyMock.replay(ngsi9Interface);
		EasyMock.expect(
				ngsi10Requestor.queryContext(
						Matchers.QueryContextRequestMatcher(util.prepareQueryContextRequest("/queryContextRequest.xml")) ,
						(URI) EasyMock.anyObject())).andReturn(
								util.prepareQueryContextResponse("/queryContextResponse.xml"));
		EasyMock.replay(ngsi10Requestor);


		QueryContextResponse qcRes = iotBrokerCore
				.queryContext(queryContextRequest);

		EasyMock.verify(ngsi9Interface);
		EasyMock.verify(ngsi10Requestor);

		assertEquals(qcRes.toString(), queryContextResponse.toString());

	}
	@Test
	public void testQueryContextChkRestriction() {
		QueryContextRequest queryContextRequest = util.prepareQueryContextRequest("/queryContextRequestWithOutRestriction.xml");
		QueryContextResponse queryContextResponse = util.prepareQueryContextResponse("/queryContextResponseWithOutRestriction.xml");

		EasyMock.expect(
				ngsi9Interface
						.discoverContextAvailability((DiscoverContextAvailabilityRequest) EasyMock
								.anyObject()))
				.andReturn(
						util.prepareDiscoverContextAvailabilityResponse("/discoverContextAvailabilityResponse.xml"));

		EasyMock.replay(ngsi9Interface);
		EasyMock.expect(
				ngsi10Requestor.queryContext(
						(QueryContextRequest) EasyMock.anyObject(),
						(URI) EasyMock.anyObject())).andReturn(
								util.prepareQueryContextResponse("/queryContextResponse.xml"));
		EasyMock.replay(ngsi10Requestor);


		QueryContextResponse qcRes = iotBrokerCore
				.queryContext(queryContextRequest);

		EasyMock.verify(ngsi9Interface);
		EasyMock.verify(ngsi10Requestor);

		assertEquals(qcRes.toString(), queryContextResponse.toString());
	}

	@Test
	public void testQueryContextChkOperationScope() {
		QueryContextRequest queryContextRequest = util.prepareQueryContextRequest("/queryContextRequestOpetaionScope.xml");
		QueryContextResponse queryContextResponse = util.prepareQueryContextResponse("/queryContextResponseWithOutRestriction.xml");

		DiscoverContextAvailabilityRequest dca= util.prepareDiscoverContextAvailabilityRequest("/discoverContextAvailabilityRequestOperationScope.xml");
		EasyMock.expect(
						ngsi9Interface
								.discoverContextAvailability(Matchers.DiscoverContextAvailabilityMatching(dca)))
						.andReturn(
								util.prepareDiscoverContextAvailabilityResponse("/discoverContextAvailabilityResponse.xml"));
		EasyMock.replay(ngsi9Interface);

		EasyMock.expect(
				ngsi10Requestor.queryContext(
						Matchers.QueryContextRequestMatcher(util.prepareQueryContextRequest("/queryContextRequestOpetaionScope.xml")) ,
						(URI) EasyMock.anyObject())).andReturn(
								util.prepareQueryContextResponse("/queryContextResponseWithOutRestriction.xml"));
		EasyMock.replay(ngsi10Requestor);


		QueryContextResponse qcRes = iotBrokerCore
				.queryContext(queryContextRequest);

		EasyMock.verify(ngsi9Interface);
		EasyMock.verify(ngsi10Requestor);

		assertEquals(qcRes.toString(), queryContextResponse.toString());
	}
	@Test
	public void testUpdateContext() {


		UpdateContextRequest updateContextRequest= util.prepareUpdateContextRequest("/updateContextRequest.xml");
		UpdateContextResponse updateContextResponseExpected= util.prepareUpdateContextResponse("/updateContextResponse.xml");

		DiscoverContextAvailabilityRequest dca= util.prepareDiscoverContextAvailabilityRequest("/discoverContextAvailabilityRequestUpdate.xml");
		EasyMock.expect(
						ngsi9Interface
								.discoverContextAvailability(Matchers.DiscoverContextAvailabilityMatching(dca)))
						.andReturn(
								util.prepareDiscoverContextAvailabilityResponse("/discoverContextAvailabilityResponseUpdate.xml"));
		EasyMock.replay(ngsi9Interface);


		EasyMock.expect(
				ngsi10Requestor.updateContext(
						Matchers.UpdateContextRequestMatcher(util.prepareUpdateContextRequest("/updateContextRequest.xml")) ,
						(URI) EasyMock.anyObject())).andReturn(
								util.prepareUpdateContextResponse("/updateContextResponse.xml"));
		EasyMock.replay(ngsi10Requestor);

		UpdateContextResponse updateContextResponseActual = iotBrokerCore.updateContext(updateContextRequest);
		System.out.println(updateContextResponseActual.toString());
		EasyMock.verify(ngsi9Interface);
		EasyMock.verify(ngsi10Requestor);

		assertEquals( updateContextResponseExpected.toString(),updateContextResponseActual.toString());
	}

}
