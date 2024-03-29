#ifndef RE_[-cmake_project_name-]_DEVICE_H
#define RE_[-cmake_project_name-]_DEVICE_H

#include <logging.h>
#include <JukeboxTypes.h>

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

  inline int getSampleRate() const { return fSampleRate; }

  void renderBatch(const TJBox_PropertyDiff *iPropertyDiffs, TJBox_UInt32 iDiffCount);

protected:
  int fSampleRate;
};

#endif //RE_[-cmake_project_name-]_DEVICE_H