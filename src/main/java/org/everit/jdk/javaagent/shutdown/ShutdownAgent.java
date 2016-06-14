/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jdk.javaagent.shutdown;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Java Agent that shuts down the JVM.
 */
public final class ShutdownAgent {

  /**
   * Exit code argument name. The JVM will exit with the provided exit code.
   */
  public static final String ARG_EXIT_CODE = "exitcode";

  /**
   * If the JVM does not exists in the specified timeout, the JVM will be halted.
   */
  public static final String ARG_EXIT_TIMEOUT = "timeout";

  public static final String ARG_HALT_CODE = "haltcode";

  public static final int DEFAULT_EXIT_CODE = 0;

  /**
   * Default timeout until the agent waits if the JVM is stopped. After that, the JVM is stopped
   * forcefully. By default, the agent waits forever.
   */
  public static final long DEFAULT_EXIT_TIMEOUT = 0;

  public static final int DEFAULT_HALT_CODE = 1;

  /**
   * Main method of the agent that is called after the agent is installed.
   *
   * @param agentArgs
   *          The arguments of the agent. Possible arguments: {@value #ARG_EXIT_TIMEOUT},
   *          {@value #ARG_EXIT_CODE} and {@value #ARG_HALT_CODE}.
   */
  public static void agentmain(final String agentArgs) {
    Map<String, String> args = splitAgentArgs(agentArgs);
    final long timeout = resolveLongArg(ARG_EXIT_TIMEOUT, DEFAULT_EXIT_TIMEOUT, args);
    final int exitCode = (int) resolveLongArg(ARG_EXIT_CODE, DEFAULT_EXIT_CODE, args);
    final int haltCode = (int) resolveLongArg(ARG_HALT_CODE, DEFAULT_HALT_CODE, args);

    new Thread(new Runnable() {

      @Override
      public void run() {
        Runtime.getRuntime().exit(exitCode);
      }
    }).start();

    if (timeout > 0) {
      Thread haltThread = new Thread(new Runnable() {

        @Override
        public void run() {
          long startTime = System.currentTimeMillis();
          long endTime = startTime;
          while (endTime - startTime < timeout) {
            try {
              Thread.sleep(timeout);
            } catch (InterruptedException e) {
              e.printStackTrace(System.err);
            }
            endTime = System.currentTimeMillis();
            if (endTime - startTime >= timeout) {
              Runtime.getRuntime().halt(haltCode);
            }
          }
        }
      });
      haltThread.setDaemon(true);
      haltThread.start();
    }
  }

  private static long resolveLongArg(final String argName, final long defaultValue,
      final Map<String, String> args) {
    String argString = args.get(argName);
    if (argString == null) {
      return defaultValue;
    }
    return Long.parseLong(argString);
  }

  private static Map<String, String> splitAgentArgs(final String agentArgs) {
    if (agentArgs == null) {
      return Collections.emptyMap();
    }

    String[] splitted = agentArgs.split(",");
    Map<String, String> result = new LinkedHashMap<String, String>();

    for (String argString : splitted) {
      String[] argParts = argString.split("=");
      String key = argParts[0];
      if (argParts.length == 1) {
        result.put(key, null);
      }
      StringBuilder value = new StringBuilder();
      for (int i = 1; i < argParts.length; i++) {
        value.append(argParts[i]);
      }
      result.put(key, value.toString());
    }

    return result;
  }

  private ShutdownAgent() {
  }
}
