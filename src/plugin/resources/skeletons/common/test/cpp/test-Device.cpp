#include <gtest/gtest.h>
#include <logging.h>
#include <re/mock/re-mock.h>
#include <re_cmake_build.h>
[-test_includes-]

using namespace re::mock;

// [-test_class_name-] - Init
TEST([-test_class_name-], Init)
{
  DLOG_F(INFO, "[-test_class_name-] - Init");

  // make sure that loguru/DCHECK_F calls throw an exception rather than aborting during test
  loguru::init_for_test();

  // load/parse info.lua, motherboard_def.lua and realtime_controller.lua
  auto c = DeviceConfig<[-test_class_name-]>::fromJBoxExport(RE_CMAKE_PROJECT_DIR);

  // instantiate the tester
  auto tester = [-tester_device_type-]<[-test_class_name-]>(c);

  // Run the first batch
  // 1. execute the rtc bindings -> instantiate [-test_class_name-]
  // 2. calls [-test_class_name-]::renderBatch (once)
  tester.nextBatch();
}
