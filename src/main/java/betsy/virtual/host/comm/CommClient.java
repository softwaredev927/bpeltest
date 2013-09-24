package betsy.virtual.host.comm;

import betsy.virtual.common.exceptions.*;
import betsy.virtual.common.messages.DeployOperation;
import betsy.virtual.common.messages.LogfileCollection;

import java.io.IOException;

/**
 * The {@link CommClient} is running on the host and offers several methods to
 * tell the server what has to be done next. This includes to send deployment
 * instructions as well as to gather log files from the remote server.
 *
 * @author Cedric Roeck
 * @version 1.0
 */
public interface CommClient {

    /**
     * Reconnect to the server.<br>
     * If the client is already connected to the server, disconnect first.
     *
     * @param timeout max time to use while connecting to the client (in ms)
     * @throws IOException thrown if the connection could not be established
     */
    public void reconnect(int timeout) throws IOException;

    /**
     * Request the log files of the engine and of the server itself from the
     * server and wait until they have been received.
     *
     * @param betsyInstallDir  the dir where the server's log files are
     * @param engineInstallDir the dir where the engine's log files are
     * @return the received log files from the server
     * @throws ChecksumException        thrown if the received log files got corrupted by the network
     *                                  transmission
     * @throws ConnectionException      thrown if the connection to the server was lost
     * @throws InvalidResponseException thrown if the server did respond with an invalid message.
     *                                  Indicating incompatible versions.
     * @throws CollectLogfileException  thrown if the log files could not be collected. Check the
     *                                  server manually to resolve the issue
     */
    public LogfileCollection getLogfilesFromServer(
            final String betsyInstallDir, final String engineInstallDir)
            throws ChecksumException, ConnectionException,
            InvalidResponseException, CollectLogfileException;

    /**
     * Send the {@link DeployOperation} to the server and wait for the
     * deployment confirmation.
     *
     * @param container deploymentOperation to be sent
     * @throws DeployException          thrown if deployment failed
     * @throws ChecksumException        thrown if the container was received corrupted at the server
     * @throws ConnectionException      thrown if connection to the server was lost
     * @throws InvalidResponseException thrown if server responded with an invalid answer. Indicating
     *                                  incompatible versions.
     */
    public void sendDeploy(DeployOperation container) throws DeployException,
            ChecksumException, ConnectionException, InvalidResponseException;

    /**
     * Check if the connection to the server is alive. Sends a PING to the
     * server and expects a PONG to be returned.
     *
     * @return true if connection is alive.
     * @throws InvalidResponseException thrown if server responded with an invalid answer. Indicating
     *                                  incompatible versions.
     */
    public boolean isConnectionAlive() throws InvalidResponseException;

    /**
     * Send the name of the current engine to the server so he knows which
     * Deployer to use.
     *
     * @param engineName name of the currently tested engine
     * @throws InvalidResponseException thrown if server responded with an invalid answer. Indicating
     *                                  incompatible versions.
     * @throws ConnectionException      thrown if the connection to the server was lost
     */
    public void sendEngineInformation(final String engineName)
            throws InvalidResponseException, ConnectionException;

    /**
     * Disconnect the client from the server.
     */
    public void disconnect();

    /**
     * Check whether the client is connected to the server and if the connection
     * is still alive.
     *
     * @return true if is connected and connection is alive
     */
    public boolean isConnected();

}
