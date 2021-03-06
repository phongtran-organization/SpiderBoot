package SpiderAgentApp;


/**
* SpiderAgentApp/AgentSideOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../idl/SpiderAgentAPI.idl
* Friday, June 1, 2018 12:29:08 AM ICT
*/

public interface AgentSideOperations 
{
  void onDownloadStartup (String appId);
  void onRenderStartup (String appId);
  void onUploadStartup (String appId);
  long getLastSyncTime (int mappingId);
  void updateLastSyntime (int mappingId, long lastSyncTime);
  void updateDownloadedVideo (SpiderAgentApp.AgentSidePackage.VideoInfo vInfo);
  void updateRenderedVideo (int jobId, int processStatus, String vRenderPath);
  void updateUploadedVideo (int videoId, int processStatus, String videoLocation);
} // interface AgentSideOperations
