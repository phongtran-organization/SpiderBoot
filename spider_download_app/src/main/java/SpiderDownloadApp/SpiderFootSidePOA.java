package SpiderDownloadApp;


/**
* SpiderDownloadApp/SpiderFootSidePOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../idl/SpiderAgentAPI.idl
* Friday, June 1, 2018 12:29:08 AM ICT
*/

public abstract class SpiderFootSidePOA extends org.omg.PortableServer.Servant
 implements SpiderDownloadApp.SpiderFootSideOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("createMappingChannel", new java.lang.Integer (0));
    _methods.put ("modifyMappingChannel", new java.lang.Integer (1));
    _methods.put ("deleteMappingChannel", new java.lang.Integer (2));
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
       case 0:  // SpiderDownloadApp/SpiderFootSide/createMappingChannel
       {
         int timerId = in.read_long ();
         String cHomeId = in.read_wstring ();
         String cMonitorId = in.read_wstring ();
         String downloadClusterId = in.read_wstring ();
         int timerInterval = in.read_long ();
         boolean $result = false;
         $result = this.createMappingChannel (timerId, cHomeId, cMonitorId, downloadClusterId, timerInterval);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 1:  // SpiderDownloadApp/SpiderFootSide/modifyMappingChannel
       {
         int timerId = in.read_long ();
         String cHomeId = in.read_wstring ();
         String cMonitorId = in.read_wstring ();
         String downloadClusterId = in.read_wstring ();
         int timerInterval = in.read_long ();
         int synStatus = in.read_long ();
         boolean $result = false;
         $result = this.modifyMappingChannel (timerId, cHomeId, cMonitorId, downloadClusterId, timerInterval, synStatus);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       case 2:  // SpiderDownloadApp/SpiderFootSide/deleteMappingChannel
       {
         int timerId = in.read_long ();
         String downloadClusterId = in.read_wstring ();
         boolean $result = false;
         $result = this.deleteMappingChannel (timerId, downloadClusterId);
         out = $rh.createReply();
         out.write_boolean ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:SpiderDownloadApp/SpiderFootSide:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public SpiderFootSide _this() 
  {
    return SpiderFootSideHelper.narrow(
    super._this_object());
  }

  public SpiderFootSide _this(org.omg.CORBA.ORB orb) 
  {
    return SpiderFootSideHelper.narrow(
    super._this_object(orb));
  }


} // class SpiderFootSidePOA
