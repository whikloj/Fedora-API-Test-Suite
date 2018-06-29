/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.spec.testsuite.versioning;

import static org.fcrepo.spec.testsuite.Constants.ORIGINAL_RESOURCE_LINK_HEADER;
import static org.fcrepo.spec.testsuite.Constants.SLUG;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.ws.rs.core.Link;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.fcrepo.spec.testsuite.AbstractTest;
import org.fcrepo.spec.testsuite.TestInfo;
import org.testng.Assert;
import org.testng.annotations.Parameters;

/**
 * @author Daniel Bernstein
 */
public class AbstractVersioningTest extends AbstractTest {

    /**
     * Authentication
     *
     * @param username The repository username
     * @param password The repository password
     */
    @Parameters({"param2", "param3"})
    public AbstractVersioningTest(final String username, final String password) {
        super(username, password);
    }

    protected URI getTimeMapUri(final Response response) {
        return getLinksOfRelTypeAsUris(response, "timemap").findFirst().get();
    }

    protected Stream<Header> getHeaders(final Response response, final String headerName) {
        return response.getHeaders()
                       .getList(headerName)
                       .stream();
    }

    protected Stream<Link> getLinksOfRelType(final Response response, final String relType) {
        return getHeaders(response, "Link")
            .flatMap(header -> Arrays.stream(header.getValue().split(",")).map(linkStr -> Link.valueOf(linkStr)))
            .filter(link -> link.getRel().equalsIgnoreCase(relType));

    }

    protected Stream<URI> getLinksOfRelTypeAsUris(final Response response, final String relType) {
        return getLinksOfRelType(response, relType)
            .map(link -> link.getUri());
    }

    protected Response createVersionedResource(final String uri, final TestInfo info) {
        final Headers headers = new Headers(
            new Header("Link", ORIGINAL_RESOURCE_LINK_HEADER),
            new Header(SLUG, info.getId()));
        return doPost(uri, headers);
    }

    protected void confirmPresenceOfHeaderValueInMultiValueHeader(final String headerName, final String headerValue,
                                                                  final Response response) {
        Assert
            .assertTrue(hasHeaderValueInMultiValueHeader(headerName, headerValue, response),
                        headerName + " with a value of " + headerValue + " must be present but is not!");
    }

    protected boolean hasHeaderValueInMultiValueHeader(final String headerName, final String headerValue,
                                                       final Response response) {
        return getHeaders(response, headerName).flatMap(header -> {
            return Arrays.stream(header.getValue().split(","));
        })
                                               .map(val -> val.trim())
                                               .filter(val -> {
                                                   return val.equalsIgnoreCase(headerValue);
                                               }).count() > 0;

    }

}
