#include <gtest/gtest.h>
#include <Device.h>
#include <logging.h>

// Device - SampleRate
TEST(Device, SampleRate)
{
  DLOG_F(INFO, "Demonstrating test capability");
  Device device{44100};
  ASSERT_EQ(44100, device.getSampleRate());
}
