#ifndef RE_BLANK_PLUGIN_DEVICE_H
#define RE_BLANK_PLUGIN_DEVICE_H

#include "logging.h"
#include "JukeboxTypes.h"

class Device
{
public:
  //! Constructor for when you don't care about sample rate
  Device() : fSampleRate{-1}
  {
    DLOG_F(INFO, "Device()");
  }

  //! Constructor with sample rate
  explicit Device(int iSampleRate) : fSampleRate{iSampleRate}
  {
    DLOG_F(INFO, "Device(%d)", iSampleRate);
  }

  void renderBatch(const TJBox_PropertyDiff *iPropertyDiffs, TJBox_UInt32 iDiffCount);

protected:
  int fSampleRate;
};

#endif //RE_BLANK_PLUGIN_DEVICE_H