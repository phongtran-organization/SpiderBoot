package SpiderAgentApp.AgentSidePackage;

/**
* SpiderAgentApp/AgentSidePackage/VideoInfoHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../idl/SpiderAgentAPI.idl
* Friday, June 1, 2018 12:29:08 AM ICT
*/

public final class VideoInfoHolder implements org.omg.CORBA.portable.Streamable
{
  public SpiderAgentApp.AgentSidePackage.VideoInfo value = null;

  public VideoInfoHolder ()
  {
  }

  public VideoInfoHolder (SpiderAgentApp.AgentSidePackage.VideoInfo initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = SpiderAgentApp.AgentSidePackage.VideoInfoHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    SpiderAgentApp.AgentSidePackage.VideoInfoHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return SpiderAgentApp.AgentSidePackage.VideoInfoHelper.type ();
  }

}
