package SpiderRenderApp;


/**
* SpiderRenderApp/SpiderFootSideOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../idl/SpiderAgentAPI.idl
* Tuesday, May 29, 2018 2:49:54 PM ICT
*/

public interface SpiderFootSideOperations 
{
  boolean createRenderJob (int jobId, SpiderRenderApp.SpiderFootSidePackage.RenderInfo vInfo);
  boolean modifyRenderJob (int jobId, SpiderRenderApp.SpiderFootSidePackage.RenderInfo vInfo);
  boolean deleteRenderJob (int jobId, SpiderRenderApp.SpiderFootSidePackage.RenderInfo vInfo);
} // interface SpiderFootSideOperations
