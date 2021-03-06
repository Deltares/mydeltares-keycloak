package nl.deltares.keycloak.storage.jpa.model;

import nl.deltares.keycloak.storage.rest.model.DownloadCallback;
import nl.deltares.keycloak.storage.rest.model.ExportCsvContent;
import org.jboss.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class DataRequestManager {

    private static final Logger logger = Logger.getLogger(DataRequestManager.class);
    private Map<String, DataRequest> dataRequests = Collections.synchronizedMap(new HashMap<>());
    private static DataRequestManager requestManager;
    private static Queue<String> pendingRequestsIds = new LinkedList<>();

    public static DataRequestManager getInstance(){
        if (requestManager == null){
            requestManager = new DataRequestManager();
        }
        return requestManager;
    }

    public DataRequest getDataRequest(String id){
        return dataRequests.get(id);
    }

    public void removeDataRequest(DataRequest dataRequest)  {
        if (dataRequest == null) throw new IllegalArgumentException("dataRequest == null");
        dataRequest.dispose();
        dataRequests.remove(dataRequest.getId());
    }

    public void addToQueue(DataRequest dataRequest){
        if (dataRequest == null) throw new IllegalArgumentException("dataRequest == null");
        if (dataRequest.getStatus() != DataRequest.STATUS.pending)
            throw new IllegalStateException(String.format("Data request %s has invalid state %s! State must be 'pending' to add to queue.", dataRequest.getId(), dataRequest.getStatus()));

        DataRequest existingRequest = dataRequests.get(dataRequest.getId());
        if (existingRequest != null && existingRequest.getStatus() == DataRequest.STATUS.running){
            throw new IllegalStateException(String.format("Data request %s is still running! Either terminate this request or wait for it to complete.", dataRequest.getId()));
        }
        dataRequests.put(dataRequest.getId(), dataRequest);
        pendingRequestsIds.add(dataRequest.getId());
        if (existingRequest != null) existingRequest.dispose();
        if (!hasRunningRequests()) {
            startNextRequest();
        }
    }

    private boolean hasRunningRequests() {
        for (DataRequest request : dataRequests.values()) {
            if (request.getStatus() == DataRequest.STATUS.running) return true;
        }
        return false;
    }

    /**
     * Clears cache
     */
    public void clear() {
        for (DataRequest value : dataRequests.values()) {
            value.dispose();
        }
        dataRequests.clear();
    }

    void fireStateChanged(DataRequest changedRequest) {
        DataRequest.STATUS status = changedRequest.getStatus();
        if (status == DataRequest.STATUS.available
                || status == DataRequest.STATUS.terminated ){
            changedRequest.setDataRequestManager(null); //stop listening
            startNextRequest();
        }
    }

    private void startNextRequest() {

        while (pendingRequestsIds.size() > 0) {
            String nextId = pendingRequestsIds.remove();
            DataRequest request = dataRequests.get(nextId);
            if (request == null) continue;
            request.setDataRequestManager(this);
            request.start();
            break;
        }
    }

    public static Response getExportDataResponse(ExportCsvContent content, Properties properties, boolean cacheExport) {

        DataRequestManager instance = DataRequestManager.getInstance();
        DataRequest dataRequest = instance.getDataRequest(content.getId());
        if (dataRequest == null ||
                dataRequest.getStatus() == DataRequest.STATUS.expired ||
                (dataRequest.getStatus() == DataRequest.STATUS.available && !dataRequest.getDataFile().exists())){
            try {
                dataRequest = new ExportCsvDataRequest(content, properties);
                if (dataRequest.getStatus() == DataRequest.STATUS.pending || !cacheExport) {
                    instance.addToQueue(dataRequest);
                }
            } catch (IOException e) {
                return Response.serverError().entity(e.getMessage()).build();
            }
        }
        DataRequest.STATUS status = dataRequest.getStatus();
        if (status == DataRequest.STATUS.available && dataRequest.getDataFile().exists()){

            DataRequest finalDataRequest = dataRequest;
            DownloadCallback callback = () -> {
                if (!cacheExport) instance.removeDataRequest(finalDataRequest);
            };
            return Response.
                    ok(getStreamingOutput(dataRequest.getDataFile(), callback)).
                    type("text/csv").
                    build();
        } else if (status == DataRequest.STATUS.terminated) {
            instance.removeDataRequest(dataRequest);
            return Response.serverError().entity(dataRequest.getErrorMessage()).build();
        } else {
            return Response.ok(dataRequest.getStatusMessage()).type("text/plain").build();
        }

    }

    static StreamingOutput getStreamingOutput(File data, DownloadCallback callback) {
        return os -> {
            try {
                Files.copy(data.toPath(), os);
                os.flush();
            } catch (Exception e) {
                logger.warn("Error serializing csv content: %s", e);
            } finally {
                callback.downloadComplete();
            }
        };
    }
}
