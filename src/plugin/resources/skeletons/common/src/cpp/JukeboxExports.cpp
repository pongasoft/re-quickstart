#include <Jukebox.h>
#include <cstring>
#include "Device.h"

/**
 * This static function is the factory to create native objects. Native objects are created in the lua file called
 * `realtime_controller.lua`.
 */
void *JBox_Export_CreateNativeObject(const char iOperation[], const TJBox_Value iParams[], TJBox_UInt32 iCount)
{
#if LOCAL_NATIVE_BUILD
  loguru::init_for_re("[[-info-medium_name-]]");
#endif
  if(std::strcmp(iOperation, "Instance") == 0)
  {
    DLOG_F(INFO, "CreateNativeObject / Instance");
    if(iCount >= 1)
    {
      TJBox_Float64 sampleRate = JBox_GetNumber(iParams[0]);
      return new Device(static_cast<int>(sampleRate));
    }
    else
    {
      return new Device();
    }
  }

#if LOCAL_NATIVE_BUILD
  ABORT_F("Unknown operation [%s] passed to JBox_Export_CreateNativeObject", iOperation);
#else
  return nullptr;
#endif
}

/**
 * This is the callback invoked by Reason/Recon to do the audio rendering. This is the "C" external API. This
 * implementation simply delegate to the C++ `Device` object which the device factory
 * (`JBox_Export_CreateNativeObject`) needs to create.
 */
void JBox_Export_RenderRealtime(void *iPrivateState, const TJBox_PropertyDiff iPropertyDiffs[], TJBox_UInt32 iDiffCount)
{
  if(!iPrivateState)
    return;

  auto device = reinterpret_cast<Device *>(iPrivateState);
  device->renderBatch(iPropertyDiffs, iDiffCount);
}
