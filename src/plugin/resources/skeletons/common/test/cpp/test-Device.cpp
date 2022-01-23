#include <gtest/gtest.h>
#include <Device.h>
#include <logging.h>
#include <re/mock/re-mock.h>
#include <re_cmake_build.h>

using namespace re::mock;

// Device - Init
TEST(Device, Init)
{
  DLOG_F(INFO, "Device - Init");

  // make sure that loguru/DCHECK_F calls throw an exception rather than aborting during test
  loguru::init_for_test();

  // load/parse info.lua, motherboard_def.lua and realtime_controller.lua
  auto c = DeviceConfig<Device>::fromJBoxExport(RE_CMAKE_PROJECT_DIR);

  // instantiate the tester
  auto tester = [-tester-device_type-]<Device>(c);

  // Run the first batch
  // 1. execute the rtc bindings -> instantiate the Device
  // 2. calls Device::renderBatch (once)
  tester.nextBatch();

  // Make sure the sample rate was set properly
  ASSERT_EQ(44100, tester.device()->getSampleRate());
}
