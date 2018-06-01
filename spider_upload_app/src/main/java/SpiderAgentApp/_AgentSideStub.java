package SpiderAgentApp;


/**
* SpiderAgentApp/_AgentSideStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../idl/SpiderAgentAPI.idl
* Friday, June 1, 2018 4:31:39 PM ICT
*/

public class _AgentSideStub extends org.omg.CORBA.portable.ObjectImpl implements SpiderAgentApp.AgentSide
{

  public void onDownloadStartup (String appId)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("onDownloadStartup", true);
                $out.write_wstring (appId);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                onDownloadStartup (appId        );
            } finally {
                _releaseReply ($in);
            }
  } // onDownloadStartup

  public void onRenderStartup (String appId)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("onRenderStartup", true);
                $out.write_wstring (appId);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                onRenderStartup (appId        );
            } finally {
                _releaseReply ($in);
            }
  } // onRenderStartup

  public void onUploadStartup (String appId)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("onUploadStartup", true);
                $out.write_wstring (appId);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                onUploadStartup (appId        );
            } finally {
                _releaseReply ($in);
            }
  } // onUploadStartup

  public long getLastSyncTime (int mappingId)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("getLastSyncTime", true);
                $out.write_long (mappingId);
                $in = _invoke ($out);
                long $result = $in.read_longlong ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return getLastSyncTime (mappingId        );
            } finally {
                _releaseReply ($in);
            }
  } // getLastSyncTime

  public void updateLastSyntime (int mappingId, long lastSyncTime)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("updateLastSyntime", true);
                $out.write_long (mappingId);
                $out.write_longlong (lastSyncTime);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                updateLastSyntime (mappingId, lastSyncTime        );
            } finally {
                _releaseReply ($in);
            }
  } // updateLastSyntime

  public void updateDownloadedVideo (SpiderAgentApp.AgentSidePackage.VideoInfo vInfo)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("updateDownloadedVideo", true);
                SpiderAgentApp.AgentSidePackage.VideoInfoHelper.write ($out, vInfo);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                updateDownloadedVideo (vInfo        );
            } finally {
                _releaseReply ($in);
            }
  } // updateDownloadedVideo

  public void updateRenderedVideo (int jobId, int processStatus, String vRenderPath)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("updateRenderedVideo", true);
                $out.write_long (jobId);
                $out.write_long (processStatus);
                $out.write_wstring (vRenderPath);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                updateRenderedVideo (jobId, processStatus, vRenderPath        );
            } finally {
                _releaseReply ($in);
            }
  } // updateRenderedVideo

  public void updateUploadedVideo (int jobId)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("updateUploadedVideo", true);
                $out.write_long (jobId);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                updateUploadedVideo (jobId        );
            } finally {
                _releaseReply ($in);
            }
  } // updateUploadedVideo

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:SpiderAgentApp/AgentSide:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _AgentSideStub
