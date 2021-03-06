package SpiderAgentApp;


/**
* SpiderAgentApp/AgentSidePOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../idl/SpiderAgentAPI.idl
* Friday, June 1, 2018 4:31:39 PM ICT
*/

public abstract class AgentSidePOA extends org.omg.PortableServer.Servant
 implements SpiderAgentApp.AgentSideOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("onDownloadStartup", new java.lang.Integer (0));
    _methods.put ("onRenderStartup", new java.lang.Integer (1));
    _methods.put ("onUploadStartup", new java.lang.Integer (2));
    _methods.put ("getLastSyncTime", new java.lang.Integer (3));
    _methods.put ("updateLastSyntime", new java.lang.Integer (4));
    _methods.put ("updateDownloadedVideo", new java.lang.Integer (5));
    _methods.put ("updateRenderedVideo", new java.lang.Integer (6));
    _methods.put ("updateUploadedVideo", new java.lang.Integer (7));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // SpiderAgentApp/AgentSide/onDownloadStartup
       {
         String appId = in.read_wstring ();
         this.onDownloadStartup (appId);
         out = $rh.createReply();
         break;
       }

       case 1:  // SpiderAgentApp/AgentSide/onRenderStartup
       {
         String appId = in.read_wstring ();
         this.onRenderStartup (appId);
         out = $rh.createReply();
         break;
       }

       case 2:  // SpiderAgentApp/AgentSide/onUploadStartup
       {
         String appId = in.read_wstring ();
         this.onUploadStartup (appId);
         out = $rh.createReply();
         break;
       }

       case 3:  // SpiderAgentApp/AgentSide/getLastSyncTime
       {
         int mappingId = in.read_long ();
         long $result = (long)0;
         $result = this.getLastSyncTime (mappingId);
         out = $rh.createReply();
         out.write_longlong ($result);
         break;
       }

       case 4:  // SpiderAgentApp/AgentSide/updateLastSyntime
       {
         int mappingId = in.read_long ();
         long lastSyncTime = in.read_longlong ();
         this.updateLastSyntime (mappingId, lastSyncTime);
         out = $rh.createReply();
         break;
       }

       case 5:  // SpiderAgentApp/AgentSide/updateDownloadedVideo
       {
         SpiderAgentApp.AgentSidePackage.VideoInfo vInfo = SpiderAgentApp.AgentSidePackage.VideoInfoHelper.read (in);
         this.updateDownloadedVideo (vInfo);
         out = $rh.createReply();
         break;
       }

       case 6:  // SpiderAgentApp/AgentSide/updateRenderedVideo
       {
         int jobId = in.read_long ();
         int processStatus = in.read_long ();
         String vRenderPath = in.read_wstring ();
         this.updateRenderedVideo (jobId, processStatus, vRenderPath);
         out = $rh.createReply();
         break;
       }

       case 7:  // SpiderAgentApp/AgentSide/updateUploadedVideo
       {
         int jobId = in.read_long ();
         this.updateUploadedVideo (jobId);
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:SpiderAgentApp/AgentSide:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public AgentSide _this() 
  {
    return AgentSideHelper.narrow(
    super._this_object());
  }

  public AgentSide _this(org.omg.CORBA.ORB orb) 
  {
    return AgentSideHelper.narrow(
    super._this_object(orb));
  }


} // class AgentSidePOA
