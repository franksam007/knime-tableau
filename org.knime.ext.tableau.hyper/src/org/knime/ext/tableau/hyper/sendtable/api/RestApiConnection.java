package org.knime.ext.tableau.hyper.sendtable.api;
/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Oct 18, 2018 (bw): created
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.AttachmentBuilder;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.ext.tableau.hyper.sendtable.api.binding.DataSourceListType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.DataSourceType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.ErrorType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.FileUploadType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.ObjectFactory;
import org.knime.ext.tableau.hyper.sendtable.api.binding.ProjectListType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.ProjectType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.SiteType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.TableauCredentialsType;
import org.knime.ext.tableau.hyper.sendtable.api.binding.TsRequest;
import org.knime.ext.tableau.hyper.sendtable.api.binding.TsResponse;

/**
 * A {@link RestApiConnection} represents a connection to the REST API of a tableau server. The connection can execute
 * requests to the tableau server and handles the authentication.
 *
 * This implementation is strongly adapted from the class RestAPIUtils from the tableau example
 * (<a href="https://github.com/tableau/rest-api-samples/blob/master/LICENSE">MIT LICENSE</a>)
 *
 * @see <a href=
 *      "https://github.com/tableau/rest-api-samples/blob/master/java/src/com/tableausoftware/documentation/api/rest/util/RestApiUtils.java">GitHub.com
 *      - RestApiUtils.java</a>
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
public class RestApiConnection {

    /** 100KB per chunk (as in the example) */
    private static final int UPLOAD_CHUNK_SIZE = 100000;

    private static final String API_VERSION = "3.1";

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private static final String SIGN_IN = "auth/signin";

    private static final String TABLEAU_AUTH_HEADER = "X-Tableau-Auth";

    private static final String QUERY_PROJECTS = "sites/{siteId}/projects";

    private static final String QUERY_DATA_SOURCES = "sites/{siteId}/datasources";

    private static final String PUBLISH_DATASOURCE = "sites/{siteId}/datasources";

    private static final String INITIATE_FILE_UPLOAD = "sites/{siteId}/fileUploads";

    private static final String APPEND_FILE_UPLOAD = "sites/{siteId}/fileUploads/{uploadSessionId}";

    private final String m_url;

    private boolean m_signedIn = false;

    private String m_token;

    private String m_siteId;

    /**
     * Creates a new connection to the tableau server with the given url.
     *
     * @param url the url to the server
     */
    public RestApiConnection(final String url) {
        m_url = url;
    }

    /**
     * Requests a sign in with the given username and password.
     *
     * @param username the tableau server username
     * @param password the tableau server password
     * @param contentUrl url of the site to sign in
     * @throws TsResponseException if the server responds with an non successful response code
     */
    public synchronized void invokeSignIn(final String username, final String password, final String contentUrl)
        throws TsResponseException {
        final String url = getUriBuilder().path(SIGN_IN).build().toString();
        final TsRequest payload = createPayloadForSignin(username, password, contentUrl);
        TsResponse response = post(url, null, payload);
        final TableauCredentialsType credentials = response.getCredentials();
        m_token = credentials.getToken();
        m_siteId = credentials.getSite().getId();
        m_signedIn = true;
    }

    /**
     * Queries the project of the connected tableau site.
     *
     * @return the list of projects
     * @throws TsResponseException if the server responds with an non successful response code
     */
    public ProjectListType invokeQueryProjects() throws TsResponseException {
        checkSignedIn();
        final String url = getUriBuilder().path(QUERY_PROJECTS).build(m_siteId).toString();
        final TsResponse response = get(url, m_token);
        return response.getProjects();
    }

    /**
     * Queries the datasources of the connected tableau site.
     *
     * @return the list of datasources
     * @throws TsResponseException if the server responds with an non successful response code
     */
    public DataSourceListType invokeQueryDatasources() throws TsResponseException {
        checkSignedIn();
        final String url = getUriBuilder().path(QUERY_DATA_SOURCES).build(m_siteId).toString();
        final TsResponse response = get(url, m_token);
        return response.getDatasources();
    }

    /**
     * Sends a datasource to the connected tableau site.
     *
     * @param projectId the id of the project to which the datasource should be added
     * @param datasourceName the name of the data source
     * @param datasourceType the type of the data source (e.g. "hyper")
     * @param dataSource the datasource file
     * @param overwrite if the datasource should be overwritten if it exists
     * @param append if the data should be appended to an existing datasource
     * @param progress a {@link ExecutionMonitor} to keep track of the upload progress
     * @return the datasource response from the server
     * @throws IOException something goes wrong while reading the file
     * @throws TsResponseException if the server responds with an non successful response code
     * @throws CanceledExecutionException if the execution was canceled
     */
    public DataSourceType invokePublishDataSourceChunked(final String projectId, final String datasourceName,
        final String datasourceType, final File dataSource, final boolean overwrite, final boolean append,
        final ExecutionMonitor progress) throws IOException, TsResponseException, CanceledExecutionException {
        checkSignedIn();

        // Initiate the file upload
        final FileUploadType fileUpload = invokeInitiateFileUpload();

        final double totalChunks = Math.ceil(1. * dataSource.length() / UPLOAD_CHUNK_SIZE);
        final byte[] buffer = new byte[UPLOAD_CHUNK_SIZE];
        int numReadBytes = 0;
        long uploadedChunks = 0;
        try (final FileInputStream inputStream = new FileInputStream(dataSource)) {
            progress.setProgress(0);
            while ((numReadBytes = inputStream.read(buffer)) != -1) {
                invokeAppendFileUpload(fileUpload.getUploadSessionId(), datasourceName, buffer, numReadBytes);
                progress.setProgress(++uploadedChunks / totalChunks);
                progress.checkCanceled();
            }
        }

        return invokePublishDataSource(fileUpload.getUploadSessionId(), datasourceName, datasourceType, projectId,
            overwrite, append);
    }

    /**
     * Invokes a file upload to the given site.
     *
     * @param credentials the connection credentials
     * @param siteId the id of the site
     * @return a {@link FileUploadType} which can be used to upload the file
     * @throws TsResponseException
     */
    private FileUploadType invokeInitiateFileUpload() throws TsResponseException {
        checkSignedIn();
        final String url = getUriBuilder().path(INITIATE_FILE_UPLOAD).build(m_siteId).toString();
        final TsResponse response = post(url, m_token, null);
        return response.getFileUpload();
    }

    /**
     * Invokes a append data for the given file upload.
     *
     * @param credentials the connection credentials
     * @param siteId the id of the site
     * @param uploadSessionId the session id of the upload
     * @param fileName name of the file uploaded
     * @param data a byte array containing the raw data
     * @param numBytes the number of bytes to send
     * @return a {@link FileUploadType}
     * @throws TsResponseException
     * @throws IOException
     */
    private FileUploadType invokeAppendFileUpload(final String uploadSessionId, final String fileName,
        final byte[] data, final int numBytes) throws TsResponseException, IOException {
        checkSignedIn();
        final String url = getUriBuilder().path(APPEND_FILE_UPLOAD).build(m_siteId, uploadSessionId).toString();
        final String body = ""; // empty body

        // Create attachments
        final List<Attachment> atts = new LinkedList<>();

        // First attachment: The xml body
        final ContentDisposition cdBody = new ContentDisposition("name=\"request_payload\"");
        atts.add(new AttachmentBuilder().id("request_payload").mediaType(MediaType.TEXT_XML).contentDisposition(cdBody)
            .object(body).build());

        try (final InputStream inputStream = new ByteArrayInputStream(data, 0, numBytes)) {
            // Second attachment: The file
            final ContentDisposition cd =
                new ContentDisposition("name=\"tableau_file\"; filename=\"" + fileName + "\"");
            atts.add(new Attachment("tableau_datasource", inputStream, cd));
            final TsResponse response = putMultipart(url, m_token, atts);
            return response.getFileUpload();
        }
    }

    private DataSourceType invokePublishDataSource(final String uploadSessionId, final String datasourceName,
        final String datasourceType, final String projectId, final boolean overwrite, final boolean append)
        throws TsResponseException {
        checkSignedIn();
        final String url = getUriBuilder().path(PUBLISH_DATASOURCE) //
            .queryParam("uploadSessionId", uploadSessionId) //
            .queryParam("datasourceType", datasourceType) //
            .queryParam("overwrite", overwrite) //
            .queryParam("append", append) //
            .build(m_siteId).toString();
        final List<Attachment> atts = new LinkedList<>();
        final TsRequest payload = createPayloadToPublishDatasource(datasourceName, projectId);
        final ContentDisposition cdBody = new ContentDisposition("name=\"request_payload\"");
        atts.add(new AttachmentBuilder().id("request_payload").mediaType(MediaType.TEXT_XML).contentDisposition(cdBody)
            .object(payload).build());
        final TsResponse response = postMultipart(url, m_token, atts);
        return response.getDatasource();
    }

    private UriBuilder getUriBuilder() {
        // TODO support multiple API versions?
        return UriBuilder.fromPath(m_url + "/api/" + API_VERSION);
    }

    private void checkSignedIn() {
        if (!m_signedIn) {
            throw new IllegalStateException(
                "Invoke login before communication with the server. This is a coding error.");
        }
    }

    private static WebClient getClient(final String url, final String token) {
        final WebClient client = WebClient.create(url);
        if (token != null) {
            client.header(TABLEAU_AUTH_HEADER, token);
        }
        return client;
    }

    private static TsResponse post(final String url, final String token, final TsRequest requestPayload)
        throws TsResponseException {
        final WebClient client = getClient(url, token);
        client.accept(MediaType.APPLICATION_XML);
        final Response response = client.post(requestPayload);
        return checkResponse(response);
    }

    private static TsResponse postMultipart(final String url, final String token, final List<Attachment> attachments)
        throws TsResponseException {
        final WebClient client = getClient(url, token);
        client.accept(MediaType.APPLICATION_XML);
        client.encoding("UTF-8");
        client.type("multipart/mixed");
        final Response response = client.post(new MultipartBody(attachments));
        return checkResponse(response);
    }

    private static TsResponse putMultipart(final String url, final String token, final List<Attachment> attachments)
        throws TsResponseException {
        final WebClient client = getClient(url, token);
        client.accept(MediaType.APPLICATION_XML);
        client.encoding("UTF-8");
        client.type("multipart/mixed");
        final Response response = client.put(new MultipartBody(attachments));
        return checkResponse(response);
    }

    private static TsResponse get(final String url, final String token) throws TsResponseException {
        final WebClient client = getClient(url, token);
        client.accept(MediaType.APPLICATION_XML);
        final Response response = client.get();
        return checkResponse(response);
    }

    private static TsResponse checkResponse(final Response response) throws TsResponseException {
        if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            try {
                final ErrorType error = response.readEntity(TsResponse.class).getError();
                final String message =
                    error.getSummary() + ": " + error.getDetail() + " (Error code: " + error.getCode() + ").";
                throw new TsResponseException(message);
            } catch (final ProcessingException e) {
                final StatusType statusInfo = response.getStatusInfo();
                throw new TsResponseException("Invalid response from server: " + statusInfo.getReasonPhrase()
                    + " (Error Code: " + statusInfo.getStatusCode() + ")", e);
            }
        }
        return response.readEntity(TsResponse.class);
    }

    /**
     * Exception which describes a response from a tableau server with a meaningful message.
     */
    public static class TsResponseException extends Exception {

        private static final long serialVersionUID = 1L;

        private TsResponseException(final String message) {
            super(message);
        }

        private TsResponseException(final String message, final Exception cause) {
            super(message, cause);
        }
    }

    // =========================================================================================================================
    // COPIED FROM:
    // https://github.com/tableau/rest-api-samples/blob/master/java/src/com/tableausoftware/documentation/api/rest/util/RestApiUtils.java
    // LICENSE: MIT
    // =========================================================================================================================

    /**
     * Creates the request payload used to sign in to the server.
     *
     * @param username the username of the user to authenticate
     * @param password the password of the user to authenticate
     * @param contentUrl the content URL for the site to authenticate to
     * @return the request payload
     */
    private static TsRequest createPayloadForSignin(final String username, final String password,
        final String contentUrl) {
        // Creates the parent tsRequest element
        TsRequest requestPayload = OBJECT_FACTORY.createTsRequest();

        // Creates the credentials element and site element
        TableauCredentialsType signInCredentials = OBJECT_FACTORY.createTableauCredentialsType();
        SiteType site = OBJECT_FACTORY.createSiteType();

        // Sets the content URL of the site to sign in to
        site.setContentUrl(contentUrl);
        signInCredentials.setSite(site);

        // Sets the name and password of the user to authenticate
        signInCredentials.setName(username);
        signInCredentials.setPassword(password);

        // Adds the credential element to the request payload
        requestPayload.setCredentials(signInCredentials);

        return requestPayload;
    }

    /**
     * Creates the request payload used to publish a datasource.
     *
     * @param datasourceName the name for the new datasource
     * @param projectId the ID of the project to publish to
     * @return the request payload
     */
    private static TsRequest createPayloadToPublishDatasource(final String datasourceName, final String projectId) {
        // Creates the parent tsRequest element
        TsRequest requestPayload = OBJECT_FACTORY.createTsRequest();

        // Creates the datasource element
        DataSourceType dataSource = OBJECT_FACTORY.createDataSourceType();

        // Creates the project element
        ProjectType project = OBJECT_FACTORY.createProjectType();

        // Sets the target project ID
        project.setId(projectId);

        // Sets the data source name
        dataSource.setName(datasourceName);

        // Sets the project
        dataSource.setProject(project);

        // Adds the workbook element to the request payload
        requestPayload.setDatasource(dataSource);

        return requestPayload;
    }
}
