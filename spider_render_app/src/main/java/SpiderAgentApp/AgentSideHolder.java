package SpiderAgentApp;

/**
* SpiderAgentApp/AgentSideHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../idl/SpiderAgentAPI.idl
* Friday, June 1, 2018 11:19:16 AM ICT
*/

public final class AgentSideHolder implements org.omg.CORBA.portable.Streamable
{
  public SpiderAgentApp.AgentSide value = null;

  public AgentSideHolder ()
  {
  }

  public AgentSideHolder (SpiderAgentApp.AgentSide initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = SpiderAgentApp.AgentSideHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    SpiderAgentApp.AgentSideHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return SpiderAgentApp.AgentSideHelper.type ();
  }

}
